package org.spoofax.interpreter.library.language;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class TransactionSemanticIndex implements ISemanticIndex {
	private ISemanticIndex index;
	private ISemanticIndex transactionIndex;
	private SemanticIndexFileDescriptor currentFile;
	private boolean clearedCurrentFile = false;
	
	public TransactionSemanticIndex(ISemanticIndex index, ISemanticIndex transactionIndex, 
			SemanticIndexFileDescriptor currentFile) {
		this.index = index;
		this.transactionIndex = transactionIndex;
		this.currentFile = currentFile;
	}
	
	public ISemanticIndex getIndex() {
		return index;
	}
	
	public ISemanticIndex getTransactionIndex() {
		return transactionIndex;
	}
	
	public boolean hasClearedCurrentFile() {
		return clearedCurrentFile;
	}
	
	public void initialize(ITermFactory factory, IOAgent agent) {
		// TODO: Should not be called
		assert false;
	}

	public SemanticIndexEntryFactory getFactory() {
		return index.getFactory();
	}

	public void add(IStrategoAppl entry, SemanticIndexFileDescriptor fileDescriptor) {
		transactionIndex.add(entry, fileDescriptor);
	}
	
	public void add(SemanticIndexEntry entry) {
		transactionIndex.add(entry);
	}

	public void addAll(IStrategoList entries, SemanticIndexFileDescriptor fileDescriptor) {
		transactionIndex.addAll(entries, fileDescriptor);
	}

	public void remove(IStrategoAppl template) {
		transactionIndex.remove(template);
	}

	public void remove(IStrategoAppl template, SemanticIndexFileDescriptor fileDescriptor) {
		transactionIndex.remove(template, fileDescriptor);
	}

	public Collection<SemanticIndexEntry> getEntries(IStrategoAppl template) {
		Collection<SemanticIndexEntry> entries1 = transactionIndex.getEntries(template);
		getReadLock().lock();
		try {
			Collection<SemanticIndexEntry> entries2 = index.getEntries(template);
			return concat(entries1, entries2);
		} finally {
			getReadLock().unlock();
		}
	}
	
	public Collection<SemanticIndexEntry> getAllEntries() {
		Collection<SemanticIndexEntry> entries1 = transactionIndex.getAllEntries();
		getReadLock().lock();
		try {
			Collection<SemanticIndexEntry> entries2 = index.getAllEntries();
			return concat(entries1, entries2);
		} finally {
			getReadLock().unlock();
		}
	}

	public Collection<SemanticIndexEntry> getEntryChildTerms(
			IStrategoAppl template) {
		Collection<SemanticIndexEntry> entries1 = transactionIndex.getEntryChildTerms(template);
		getReadLock().lock();
		try {
			Collection<SemanticIndexEntry> entries2 = index.getEntryChildTerms(template);
			return concat(entries1, entries2);
		} finally {
			getReadLock().unlock();
		}
	}

	public Collection<SemanticIndexEntry> getEntriesInFile(
			SemanticIndexFileDescriptor fileDescriptor) {
		Collection<SemanticIndexEntry> entries1 = transactionIndex.getEntriesInFile(fileDescriptor);
		getReadLock().lock();
		try {
			Collection<SemanticIndexEntry> entries2 = index.getEntriesInFile(fileDescriptor);
			return concat(entries1, entries2);
		} finally {
			getReadLock().unlock();
		}
	}
	
	public SemanticIndexFile getFile(SemanticIndexFileDescriptor fileDescriptor) {
		// TODO: Does this get the right file?
		getReadLock().lock();
		try {
			return index.getFile(fileDescriptor);
		} finally {
			getReadLock().unlock();
		}
	}
	
	public SemanticIndexFileDescriptor getFileDescriptor(IStrategoTerm fileTerm) {
		// TODO: Does this get the right file descriptor?
		getReadLock().lock();
		try {
			return index.getFileDescriptor(fileTerm);
		} finally {
			getReadLock().unlock();
		}
	}

	public void removeFile(IStrategoTerm fileTerm) {
		removeFile(transactionIndex.getFileDescriptor(fileTerm));
	}
	
	public void removeFile(SemanticIndexFileDescriptor fileDescriptor) {
		assert fileDescriptor.equals(currentFile);
		
		clearedCurrentFile = true;
		transactionIndex.removeFile(fileDescriptor);
	}

	public Collection<SemanticIndexFile> getAllFiles() {
		// TODO: No duplicates?
		Collection<SemanticIndexFile> files1 = transactionIndex.getAllFiles();
		getReadLock().lock();
		try {
			Collection<SemanticIndexFile> files2 = index.getAllFiles();
			return concat(files1, files2);
		} finally {
			getReadLock().unlock();
		}
	}
	
	public Collection<SemanticIndexFileDescriptor> getAllFileDescriptors() {
		// TODO: No duplicates?
		Collection<SemanticIndexFileDescriptor> files1 = transactionIndex.getAllFileDescriptors();
		getReadLock().lock();
		try {
			Collection<SemanticIndexFileDescriptor> files2 = index.getAllFileDescriptors();
			return concat(files1, files2);
		} finally {
			getReadLock().unlock();
		}
	}

	public void clear() {
		// Should not be called.
		assert false;
		transactionIndex.clear();
	}

	public IStrategoTerm toTerm(boolean includePositions) {
		// TODO: Transaction data not stored?
		return index.toTerm(includePositions);
	}
	
	private <T> Collection<T> concat(Collection<T> c1, Collection<T> c2) {
		// TODO: Make something like CompositeCollection so nothing has to be copied?
		List<T> l = new ArrayList<T>(c1.size() + c2.size());
		l.addAll(c1);
		l.addAll(c2);
		
		return l;
	}
	
	private Lock getReadLock() {
		return SemanticIndexManager.getTransactionLock().readLock();
	}
}