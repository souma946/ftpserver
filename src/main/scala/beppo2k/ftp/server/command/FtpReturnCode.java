package beppo2k.ftp.server.command;

public enum FtpReturnCode {
	RESTART_MARKER_REPLY(110),
	SERVICE_READY_IN_NNN_MINUTES(120),
	DATA_CONNECTION_ALREADY_OPEN(125),
	FILE_STATUS_OKAY(150),
	COMMAND_OKAY(200),
	COMMAND_NOT_IMPLEMENTED_INAPPROPRIATE_COMMAND(202),
	SYSTEM_STATUS(211),
	DIRECTORY_STATUS(212),
	FILE_STATUS(213),
	HELP_MESSAGE(214),
	NAME_SYSTEM_TYPE(215),
	SERVICE_READY_FOR_NEW_USER(220),
	SERVICE_CLOSING_CONTROL_CONNECTION(221),
	DATA_CONNECTION_OPEN(225),
	CLOSING_DATA_CONNECTION(226),
	ENTERING_PASSIVE_MODE(227),
	USER_LOGGED_IN(230),
	REQUESTED_FILE_ACTION_OKAY(250),
	PATHNAME_CREATED(257),
	USER_NAME_OKAY(331),
	NEED_ACCOUNT_FOR_LOGIN(332),
	REQUESTED_FILE_ACTION_PENDING_FURTHER_INFORMATION(350),
	SERVICE_NOT_AVAILABLE(421),
	CANT_OPEN_DATA_CONNECTION(425),
	CONNECTION_CLOSED(426),
	REQUESTED_FILE_ACTION_NOT_TAKEN(450),
	REQUESTED_ACTION_ABORTED_LOCAL_ERROR_IN_PROCESSING(451),
	REQUESTED_ACTION_NOT_TAKEN_DISKSPACE(452),
	SYNTAX_ERROR(500),
	SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS(501),
	COMMAND_NOT_IMPLEMENTED(502),
	BAD_SEQUENCE_OF_COMMANDS(503),
	COMMAND_NOT_IMPLEMENTED_FOR_THAT_PARAMETER(504),
	NOT_LOGGED_IN(530),
	NEED_ACCOUNT_FOR_STORING_FILES(532),
	REQUESTED_ACTION_NOT_TAKEN_PERMISSION_OR_SYSTEM(550),
	REQUESTED_ACTION_ABORTED_PAGE_TYPE_UNKNOWN(551),
	REQUESTED_FILE_ACTION_ABORTED(552),
	REQUESTED_ACTION_NOT_TAKEN_WRONG_FILE_NAME(553);

	private int code;

	private FtpReturnCode(int code){
		this.code = code;
	}

	public int getCode(){
		return this.code;
	}

	public byte[] getCodeAsBytes(){
		return String.format("%d" , this.code).getBytes();
	}
}
