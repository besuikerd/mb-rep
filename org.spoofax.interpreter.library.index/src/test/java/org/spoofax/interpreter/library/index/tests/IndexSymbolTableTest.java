package org.spoofax.interpreter.library.index.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spoofax.interpreter.library.index.IndexEntry;
import org.spoofax.interpreter.library.index.IndexPartition;
import org.spoofax.interpreter.library.index.IndexPartitionDescriptor;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;

@RunWith(value = Parameterized.class)
public class IndexSymbolTableTest extends IndexTest {
    private boolean startTransaction;

    public IndexSymbolTableTest(boolean startTransaction) {
        this.startTransaction = startTransaction;
    }

    private void startTransaction() {
        if(startTransaction)
            doStartTransaction();
    }

    private void endTransaction() {
        if(startTransaction)
            doEndTransaction();
    }

    @Before
    public void setUp() {
        index.clearAll();
    }

    @Test
    public void files() {
        IStrategoTerm fileTerm1 = file("a/b/c");
        IStrategoTerm fileTerm2 = file("a/b/c", "some", "element");

        startTransaction();

        IndexPartitionDescriptor file1 = setupIndex(fileTerm1);
        IndexPartitionDescriptor file2 = setupIndex(fileTerm2);

        IndexPartitionDescriptor ret1 = index.getPartitionDescriptor(fileTerm1);
        IndexPartitionDescriptor ret2 = index.getPartitionDescriptor(fileTerm2);

        // Files have not been added yet.
        Collection<IndexPartitionDescriptor> all1 = index.getAllPartitionDescriptors();
        assertFalse(all1.contains(ret1));
        assertFalse(all1.contains(ret2));

        assertEquals(ret1, file1);
        assertEquals(ret1.toTerm(factory), file1.toTerm(factory));
        assertEquals(ret2, file2);
        assertEquals(ret2.toTerm(factory), file2.toTerm(factory));

        IndexPartition retf1 = index.getPartition(file1);
        IndexPartition retf2 = index.getPartition(file2);

        assertEquals(ret1, retf1.getDescriptor());
        assertEquals(retf1.toTerm(factory), file1.toTerm(factory));
        assertEquals(ret2, retf2.getDescriptor());
        assertEquals(retf2.toTerm(factory), file2.toTerm(factory));

        // Files have been added by calling getFile.
        Collection<IndexPartitionDescriptor> all2 = index.getAllPartitionDescriptors();
        assertTrue(all2.contains(ret1));
        assertTrue(all2.contains(ret2));

        endTransaction();
    }

    @Test
    public void getEntries() {
        IStrategoAppl def = def("Class", "java", "lang", "String");
        IStrategoAppl type = type(constructor("Type", str("String")), "Class", "java", "lang", "String");
        IStrategoAppl defData = defData(constructor("Type"), str("String"), "Class", "java", "lang", "String");

        startTransaction();

        assertEquals(0, index.get(def).size());
        assertEquals(0, index.get(type).size());
        assertEquals(0, index.get(defData).size());

        index.add(def, file);
        index.add(type, file);
        index.add(defData, file);

        Collection<IndexEntry> ret1 = index.get(def);
        Collection<IndexEntry> ret2 = index.get(type);
        Collection<IndexEntry> ret3 = index.get(defData);

        endTransaction();

        assertTrue(matchAll(ret1, def));
        assertTrue(matchAll(ret2, type));
        assertTrue(matchAll(ret3, defData));
        assertFalse(matchAll(ret1, type));
        assertFalse(matchAll(ret1, defData));
        assertFalse(matchAll(ret2, def));
        assertFalse(matchAll(ret2, defData));
        assertFalse(matchAll(ret3, def));
        assertFalse(matchAll(ret3, type));
    }

    @Test
    public void duplicateAddAndGetEntries() {
        IStrategoAppl def = def("Entity", "CRM", "Person");
        IStrategoAppl read = read("Function", "CRM", "Person", "GetName");
        IStrategoAppl longTerm =
            longTerm(str("Entity"), str("CRM"), str("Person"), "Function", "CRM", "Person", "GetName");

        startTransaction();

        assertEquals(0, index.get(def).size());
        assertEquals(0, index.get(read).size());
        assertEquals(0, index.get(longTerm).size());

        index.add(def, file);
        index.add(def, file);
        index.add(def, file);
        index.add(read, file);
        index.add(read, file);
        index.add(longTerm, file);

        Collection<IndexEntry> ret1 = index.get(def);
        Collection<IndexEntry> ret2 = index.get(read);
        Collection<IndexEntry> ret3 = index.get(longTerm);

        assertEquals(3, ret1.size());
        assertEquals(2, ret2.size());
        assertEquals(1, ret3.size());

        assertTrue(matchAll(ret1, def));
        assertTrue(matchAll(ret2, read));
        assertTrue(matchAll(ret3, longTerm));
        assertFalse(matchAll(ret1, read));
        assertFalse(matchAll(ret1, longTerm));
        assertFalse(matchAll(ret2, def));
        assertFalse(matchAll(ret2, longTerm));
        assertFalse(matchAll(ret3, def));
        assertFalse(matchAll(ret3, read));

        // Add entries from ret2 again using the other add function.
        // Need to make a copy of ret2, because ret2 is a view over the index and
        // could cause a ConcurrentModificationException
        for(IndexEntry entry : ret2.toArray(new IndexEntry[0]))
            index.add(entry);

        Collection<IndexEntry> ret4 = index.get(read);

        endTransaction();

        assertEquals(4, ret4.size());
        assertTrue(matchAll(ret4, read));
        assertFalse(matchAll(ret4, def));
        assertFalse(matchAll(ret4, longTerm));
    }

    @Test
    public void addAllAndGetAllEntries() {
        IStrategoAppl def = def("Class", "java", "lang", "String");
        IStrategoAppl type = type(constructor("Type", str("String")), "Class", "java", "lang", "String");
        IStrategoAppl defData = defData(constructor("Type"), str("String"), "Class", "java", "lang", "String");
        IStrategoList all = factory.makeList(def, type, defData);

        startTransaction();

        assertEquals(0, index.get(def).size());
        assertEquals(0, index.get(type).size());
        assertEquals(0, index.get(defData).size());

        index.addAll(all, file);

        Collection<IndexEntry> ret = index.getAll();

        endTransaction();

        assertTrue(containsEntry(ret, def));
        assertTrue(containsEntry(ret, type));
        assertTrue(containsEntry(ret, defData));
        assertFalse(containsEntry(ret, all));
    }

    @Test
    public void getChildrenEntries() {
        IStrategoAppl classDef = def("Class", "java", "lang", "String");
        IStrategoAppl methodDef1 = def("Method", "java", "lang", "String", "charAt");
        IStrategoAppl methodDef2 = def("Method", "java", "lang", "String", "getBytes");
        IStrategoAppl fieldDef = def("Field", "java", "lang", "String", "length");

        IStrategoAppl methodsTemplate = def("Method", "java", "lang", "String");
        IStrategoAppl fieldsTemplate = def("Field", "java", "lang", "String");

        startTransaction();

        assertEquals(0, index.get(classDef).size());
        assertEquals(0, index.get(methodDef1).size());
        assertEquals(0, index.get(methodDef2).size());
        assertEquals(0, index.get(fieldDef).size());
        assertEquals(0, index.getChildren(methodsTemplate).size());
        assertEquals(0, index.getChildren(fieldsTemplate).size());

        index.add(classDef, file);
        index.add(methodDef1, file);
        index.add(methodDef2, file);
        index.add(fieldDef, file);

        Collection<IndexEntry> ret1 = index.getChildren(methodsTemplate);
        Collection<IndexEntry> ret2 = index.getChildren(fieldsTemplate);

        endTransaction();

        assertEquals(2, ret1.size());
        assertEquals(1, ret2.size());

        assertTrue(containsEntry(ret1, methodDef1));
        assertTrue(containsEntry(ret1, methodDef2));
        assertFalse(containsEntry(ret1, fieldDef));
        assertFalse(containsEntry(ret1, classDef));

        assertFalse(containsEntry(ret2, methodDef1));
        assertFalse(containsEntry(ret2, methodDef2));
        assertTrue(containsEntry(ret2, fieldDef));
        assertFalse(containsEntry(ret2, classDef));
    }

    @Test
    public void getEntriesInFileAndRemoveFile() {
        IStrategoTerm fileTerm1 = file("TestFile", "Partition", "1");
        IndexPartitionDescriptor file1 = setupIndex(fileTerm1);
        IndexPartitionDescriptor file2 = setupIndex(file("TestFile", "Partition", "2"));

        IStrategoAppl def1 = def("Entity", "CRM", "Person");
        IStrategoAppl read = read("Function", "CRM", "Person", "GetName");

        IStrategoAppl def2 = def("Class", "java", "lang", "String");
        IStrategoAppl type = type(constructor("Type", str("String")), "Class", "java", "lang", "String");

        startTransaction();

        assertEquals(0, index.get(def1).size());
        assertEquals(0, index.get(read).size());
        assertEquals(0, index.get(def2).size());
        assertEquals(0, index.get(type).size());
        assertEquals(0, index.getInPartition(file1).size());
        assertEquals(0, index.getInPartition(file2).size());

        index.add(def1, file1);
        index.add(read, file1);
        index.add(def2, file2);
        index.add(type, file2);

        Collection<IndexEntry> ret1 = index.getInPartition(file1);
        Collection<IndexEntry> ret2 = index.getInPartition(file2);

        assertEquals(2, ret1.size());
        assertEquals(2, ret2.size());

        assertTrue(containsEntry(ret1, def1));
        assertTrue(containsEntry(ret1, read));
        assertFalse(containsEntry(ret1, def2));
        assertFalse(containsEntry(ret1, type));

        assertFalse(containsEntry(ret2, def1));
        assertFalse(containsEntry(ret2, read));
        assertTrue(containsEntry(ret2, def2));
        assertTrue(containsEntry(ret2, type));

        for(IndexEntry entry : ret1) {
            assertSame(entry.getPartition(), file1);
            assertNotSame(entry.getPartition(), file2);
        }
        for(IndexEntry entry : ret2) {
            assertNotSame(entry.getPartition(), file1);
            assertSame(entry.getPartition(), file2);
        }

        index.clearPartition(fileTerm1);
        assertEquals(0, index.getInPartition(file1).size());
        assertEquals(2, index.getInPartition(file2).size());

        index.clearPartition(file2);
        assertEquals(0, index.getInPartition(file2).size());

        endTransaction();
    }

    @Test
    public void getPartitionsOf() {
        IStrategoTerm fileTerm1 = file("TestFile", "Partition", "1");
        IndexPartitionDescriptor file1 = setupIndex(fileTerm1);
        IndexPartitionDescriptor file2 = setupIndex(file("TestFile", "Partition", "2"));

        IStrategoAppl def = def("Entity", "CRM", "Person");
        IStrategoAppl read = read("Function", "CRM", "Person", "GetName");
        IStrategoAppl longTerm =
            longTerm(str("Entity"), str("CRM"), str("Person"), "Function", "CRM", "Person", "GetName");
        IStrategoAppl defData = defData(constructor("Type"), str("String"), "Class", "java", "lang", "String");

        startTransaction();

        assertEquals(0, index.get(def).size());
        assertEquals(0, index.get(read).size());
        assertEquals(0, index.get(longTerm).size());
        assertEquals(0, index.get(defData).size());
        assertEquals(0, index.getInPartition(file1).size());
        assertEquals(0, index.getInPartition(file2).size());

        index.add(def, file1);
        index.add(def, file2);
        index.add(def, file1);
        index.add(read, file2);
        index.add(read, file2);
        index.add(longTerm, file1);

        Collection<IndexPartitionDescriptor> ret1 = index.getPartitionsOf(def);
        Collection<IndexPartitionDescriptor> ret2 = index.getPartitionsOf(read);
        Collection<IndexPartitionDescriptor> ret3 = index.getPartitionsOf(longTerm);
        Collection<IndexPartitionDescriptor> ret4 = index.getPartitionsOf(defData);

        endTransaction();

        assertTrue(containsPartitionDescriptor(ret1, file1));
        assertTrue(containsPartitionDescriptor(ret1, file2));
        assertFalse(containsPartitionDescriptor(ret2, file1));
        assertTrue(containsPartitionDescriptor(ret2, file2));
        assertTrue(containsPartitionDescriptor(ret3, file1));
        assertFalse(containsPartitionDescriptor(ret3, file2));
        assertFalse(containsPartitionDescriptor(ret4, file1));
        assertFalse(containsPartitionDescriptor(ret4, file2));
    }

    @Test
    public void clear() {
        IndexPartitionDescriptor file1 = setupIndex(file("TestFile", "Partition", "1"));
        IndexPartitionDescriptor file2 = setupIndex(file("TestFile", "Partition", "2"));

        IStrategoAppl readAll = readAll("Str", "Class", "java", "lang");

        startTransaction();

        assertEquals(0, index.getAll().size());

        index.add(readAll, file1);
        index.add(readAll, file2);

        assertEquals(2, index.getAll().size());

        index.clearAll();

        assertEquals(0, index.getAll().size());

        endTransaction();
    }
}