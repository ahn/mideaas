package org.vaadin.mideaas.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

import org.apache.commons.io.FileUtils;
import org.vaadin.aceeditor.ServerSideDocDiff;

public class ProjectLog {
	
	enum Type {
		CREATED,
		REMOVED,
		LOADED_FROM_DISK,
		EDIT,
		CHAT,
		OPEN_FILE,
		CLOSE_FILE
	}
	
	public static abstract class LogItem {
		public final Date timestamp;
		private LogItem() {
			timestamp = new Date();
		}
		public String logString() {
			return timestamp.getTime() + " " + getType() + " " + logContentString();
		}
		abstract Type getType();
		abstract String logContentString();
	}
	
	public static class SimpleLogItem extends LogItem {
		private final Type type;
		private final String str;
		private SimpleLogItem(Type type, String str) {
			this.type = type;
			this.str = str;
		}
		public SimpleLogItem(Type type) {
			this(type, "");
		}
		@Override
		Type getType() {
			return type;
		}

		@Override
		String logContentString() {
			return str;
		}
		
	}
	
	public static class UserEditLogItem extends LogItem {
		public final String filename;
		public final String anonCode;
		public final String userName;
		public final Operation op;
		public final int editLen;
		public final int editPos;
		public final int textLen;
		public UserEditLogItem(String filename, String anonCode,
				String userName, Operation op, int editLen, int editPos,
				int textLen) {
			this.filename = filename;
			this.anonCode = anonCode;
			this.userName = userName;
			this.op = op;
			this.editLen = editLen;
			this.editPos = editPos;
			this.textLen = textLen;
		}
		@Override
		public String logContentString() {
			return filename+";"+anonCode+";"+userName+";"+
					op+";"+editLen+";"+editPos+";"+textLen;
		}
		@Override
		public Type getType() {
			return Type.EDIT;
		}
	}
	
	public static class ChatLogItem extends LogItem {

		public final String userId;
		public final String message;
		private ChatLogItem(String userId, String message) {
			this.userId = userId;
			this.message = message;
		}
		@Override
		public String logContentString() {
			return userId+";"+message;
		}
		@Override
		public Type getType() {
			return Type.CHAT;
		}
	}
	
	public static class FileOpenLogItem extends LogItem {
		public final ProjectFile file;
		public final long collaboratorId;
		public final User user;
		private FileOpenLogItem(ProjectFile file, long collaboratorId, User user) {
			this.file = file;
			this.collaboratorId = collaboratorId;
			this.user = user;
		}
		@Override
		Type getType() {
			return Type.OPEN_FILE;
		}
		@Override
		String logContentString() {
			return file.getName()+";"+collaboratorId+";"+user.getUserId()+";"+user.getName();
		}
	}
	
	public static class FileCloseLogItem extends LogItem {
		public final ProjectFile file;
		public final long collaboratorId;
		public final User user;
		private FileCloseLogItem(ProjectFile file, long collaboratorId, User user) {
			this.file = file;
			this.collaboratorId = collaboratorId;
			this.user = user;
		}
		@Override
		Type getType() {
			return Type.CLOSE_FILE;
		}
		@Override
		String logContentString() {
			return file.getName()+";"+collaboratorId+";"+user.getUserId()+";"+user.getName();
		}
	}

	private static File logDir;
	
	private final ArrayList<LogItem> log = new ArrayList<LogItem>();
	private final File file;
	
	public ProjectLog(String projectName) {
		file = isLogDirSet() ? logFileForProject(projectName) : null;
	}
	
	public static synchronized void setLogDir(File dir) {
		logDir = dir;
	}
	
	public static synchronized boolean isLogDirSet() {
		return logDir!=null;
	}

	private static synchronized File logFileForProject(String projectName) {
		return new File(logDir, projectName+".log");
	}
	
	public void logCreated() {
		log(new SimpleLogItem(Type.CREATED));
	}
	
	public void logLoadedFromDisk(File f) {
		log(new SimpleLogItem(Type.LOADED_FROM_DISK, f.getAbsolutePath()));
	}
	
	public void logRemoved() {
		log(new SimpleLogItem(Type.REMOVED));
	}

	public void logChat(String userId, String message) {
		log(new ChatLogItem(userId, message));
	}

	public void logUserEdit(String filename, String anonCode, String userName, Operation op, int editLen, int editPos,
			int fileLen) {
		log(new UserEditLogItem(filename, anonCode, userName, op, editLen, editPos, fileLen));
	}
	
	public void logUserEdit(String filename, User user, ServerSideDocDiff diff, int docLen) {
		List<Patch> patches = diff.getPatches();
		String anonCode = user instanceof ExperimentUser ? ((ExperimentUser)user).getAnonymizerCode() : "";
		String userName = user==null ? "" : user.getName();
		for (Patch p : patches) {
			for (Diff d : p.diffs) {
				if (d.operation.equals(Operation.EQUAL)) {
					continue;
				}
				int ol = d.text==null ? 0 : d.text.length();
				logUserEdit(filename, anonCode, userName, d.operation, ol, p.start1, docLen);
			}
		}
	}
	
	public void logOpenFile(ProjectFile f, long collabId, User user) {
		log(new FileOpenLogItem(f, collabId, user));	
	}
	
	public void logCloseFile(ProjectFile f, long collabId, User user) {
		log(new FileCloseLogItem(f, collabId, user));
	}
	
	
	synchronized private void log(LogItem item) {
		log.add(item);
		if (file!=null) {
			logToFile(item);
		}
	}
	
	private void logToFile(LogItem item) {
		try {
			FileUtils.writeLines(file, Collections.singletonList(item.logString()), true);
		} catch (IOException e) {
			System.err.println("WARNING: could not write log to "+file);
		}
	}

	

	
	
}
