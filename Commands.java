import static java.lang.System.out;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

public class Commands {

	/* This method determines what type of command the userCommand is and
	 * calls the appropriate method to parse the userCommand String.
	 */
	public static void parseUserCommand (String userCommand) {

		/* Clean up command string so that each token is separated by a single space */
		userCommand = userCommand.replaceAll("\n", " ");    // Remove newlines
		userCommand = userCommand.replaceAll("\r", " ");    // Remove carriage returns
		userCommand = userCommand.replaceAll(",", " , ");   // Tokenize commas
		userCommand = userCommand.replaceAll("\\(", " ( "); // Tokenize left parentheses
		userCommand = userCommand.replaceAll("\\)", " ) "); // Tokenize right parentheses
		userCommand = userCommand.replaceAll("( )+", " ");  // Reduce multiple spaces to a single space

		/* commandTokens is an array of Strings that contains one lexical token per array
		 * element. The first token can be used to determine the type of command
		 * The other tokens can be used to pass relevant parameters to each command-specific
		 * method inside each case statement
		 */
		ArrayList<String> commandTokens = new ArrayList<String>(Arrays.asList(userCommand.split(" ")));

		/*
		 *  This switch handles a very small list of hard-coded commands from SQL syntax.
		 *  You will want to rewrite this method to interpret more complex commands.
		 */
		//System.out.println(commandTokens.get(0).toLowerCase());
		switch (commandTokens.get(0).toLowerCase()) {
			case "show":
				System.out.println("Case: SHOW");
				show(commandTokens);
				break;
			case "select":
				System.out.println("Case: SELECT");
				parseQuery(commandTokens);
				break;
			case "create":
				System.out.println("");
				System.out.println("Case: CREATE TABLE");
				parseCreateTable(userCommand);
				break;
			case "insert":
				System.out.println("Case: INSERT");
				parseInsert(commandTokens);
				break;
			case "delete":
				System.out.println("Case: DELETE");
				parseDelete(commandTokens);
				break;
			case "update":
				System.out.println("Case: UPDATE");
				parseUpdate(commandTokens);
				break;
			case "drop":
				System.out.println("Case: DROP");
				dropTable(commandTokens);
				break;
			case "help":
				help();
				break;
			case "version":
				displayVersion();
				break;
			case "exit":
				Settings.setExit(true);
				break;
			case "quit":
				Settings.setExit(true);
				break;
			default:
				System.out.println("I didn't understand the command: \"" + userCommand + "\"");
				break;
		}
	}

	public static void displayVersion() {
		System.out.println("DavisBaseLite Version " + Settings.getVersion());
		System.out.println(Settings.getCopyright());
	}

	public static void parseCreateTable(String command) {
		/* TODO: Before attempting to create new table file, check if the table already exists */

		command=command.trim();
		System.out.println("Stub: parseCreateTable method");
		System.out.println("Command: " + command);

		ArrayList<String> commandTokens = commandStringToTokenList(command);

		/* Extract the table name from the command string token list */
		String tableFileName = commandTokens.get(2) + ".tbl";

		/* YOUR CODE GOES HERE */
		int openBracketIndex = command.toLowerCase().indexOf("(");
		if(openBracketIndex == -1) {
			System.out.println("The query must be of the form CREATE TABLE table_name ( column_name data_type)");
			return;
		}

		if(!command.endsWith(")")){
			System.out.println("The query must be of the form CREATE TABLE table_name ( column_name data_type)");
			return;
		}

		String detailsInBrackets = command.substring(openBracketIndex + 1, command.length()-1);
		boolean hasPrimaryKey = false;
		ArrayList<Column> columns = new ArrayList<>();
		String[] columnsList = detailsInBrackets.split(",");

		for(String columnEntry : columnsList){
			columnEntry=columnEntry.trim();
			String primaryKeyString = "primary key";
			Column column = new Column();
			String notNullString = "not null";
			boolean isNull = true;
			if(columnEntry.toLowerCase().endsWith(primaryKeyString)){
				columnEntry = columnEntry.substring(0, columnEntry.length() - primaryKeyString.length()).trim();
			}
			else if(columnEntry.toLowerCase().endsWith(notNullString)){
				columnEntry = columnEntry.substring(0, columnEntry.length() - notNullString.length()).trim();
				isNull = false;
			}

			String[] parts = columnEntry.split(" ");
			String name;
			if(parts.length > 2){
				System.out.println("Expected column format <name> <datatype> [PRIMARY KEY | NOT NULL]");
			}
			Datatypes type;
			if(parts.length > 1){
				name = parts[0].trim();
				column.name = name;
				//System.out.println(name + parts[1] + parts[0]);
				type = ReturnDataType(parts[1].trim().toLowerCase());
				column.type = type;
				if(type == null){
					System.out.println("Unrecognised data type " + parts[1]);
				}
			}
			columns.add(column);
		}

		for(int i=0; i<columns.size();i++){
			//System.out.println(columns.get(i).name);
			//System.out.println(columns.get(i).type.toString());
		}

		/*  Code to create a .tbl file to contain table data */
		try {
			/*  Create RandomAccessFile tableFile in read-write mode.
			 *  Note that this doesn't create the table file in the correct directory structure
			 */

			/* Create a new table file whose initial size is one page (i.e. page size number of bytes) */

			RandomAccessFile tableFile = new RandomAccessFile("data/user_data/" + tableFileName, "rw");
			//tableFile.setLength(Settings.getPageSize());

			tableFile.writeBytes(String.valueOf(columns.size()));
			//System.out.println(columns.size());
			for(int i=0; i<columns.size();i++){
				tableFile.writeBytes("#");
				tableFile.writeBytes(columns.get(i).name);
				tableFile.writeBytes("#");
				for(int j=0;j<18-columns.get(i).name.length();j++){
					tableFile.writeByte(20);
				}
				tableFile.writeBytes("#");
				tableFile.writeBytes(columns.get(i).type.toString());
				tableFile.writeBytes("#");
				for(int j=0;j<18-columns.get(i).type.toString().length();j++){
					tableFile.writeByte(20);
				}
			}
			System.out.println("");
			System.out.println(commandTokens.get(2).toUpperCase() + " table has been created.");
			tableFile.close();

			RandomAccessFile tableFile2 = new RandomAccessFile("data/user_data/" + tableFileName, "rw");
			//long  Filesize=tableFile2.length();
			tableFile2.seek(0);
			//long num_of_records= Filesize/row_count;
			String str = tableFile2.readLine();
			//System.out.println(str);
			tableFile2.close();


//			/* Write page header with initial configuration */
//			tableFile.seek(0);
//			tableFile.writeInt(0x0D);       // Page type
//			tableFile.seek(0x02);
//			tableFile.writeShort(0x01FF);   // Offset beginning of cell content area
//			tableFile.seek(0x06);
//			tableFile.writeInt(0xFFFFFFFF); // Sibling page to the right
//			tableFile.seek(0x0A);
//			tableFile.writeInt(0xFFFFFFFF); // Parent page
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		/*  Code to insert an entry in the TABLES meta-data for this new table.
		 *  i.e. New row in davisbase_tables if you're using that mechanism for meta-data.
		 */

		/*  Code to insert entries in the COLUMNS meta data for each column in the new table.
		 *  i.e. New rows in davisbase_columns if you're using that mechanism for meta-data.
		 */
	}

	private static Datatypes ReturnDataType(String dataTypeString) {
		switch(dataTypeString){
			case "tinyint": return Datatypes.TINYINT;
			case "smallint": return Datatypes.SMALLINT;
			case "int": return Datatypes.INT;
			case "bigint": return Datatypes.BIGINT;
			case "real": return Datatypes.REAL;
			case "double": return Datatypes.DOUBLE;
			case "datetime": return Datatypes.DATETIME;
			case "date": return Datatypes.DATE;
			case "text": return Datatypes.TEXT;
			case "long": return Datatypes.LONG;
			case "year": return Datatypes.YEAR;
			case "float": return Datatypes.FLOAT;
			case "time": return Datatypes.TIME;
		}

		return null;
	}

	public static void show(ArrayList<String> commandTokens) {
		System.out.println("Command: " + tokensToCommandString(commandTokens));
		System.out.println("Stub: This is the show method");
		/* TODO: Your code goes here */
		File directory = new File("data/user_data/");
		File[] filesArray = directory.listFiles();
		//sort all files
		Arrays.sort(filesArray);
		System.out.println("Tables");
		System.out.println("---------");
		//print the sorted values
		for (File file : filesArray) {
			if (file.isFile()) {
				System.out.println(file.getName().substring(0, file.getName().length()-4));
			}
		}
	}

	/*
	 *  Stub method for inserting a new record into a table.
	 */
	public static void parseInsert (ArrayList<String> commandTokens) {
		System.out.println("Command: " + tokensToCommandString(commandTokens));
		System.out.println("Stub: This is the insertRecord method");
		/* TODO: Your code goes here */
		String tableName = "";
		String columns = "";

		int valuesIndex = tokensToCommandString(commandTokens).toLowerCase().indexOf("values");
		if(valuesIndex == -1) {
			System.out.println("Expected VALUES keyword");
			return;
		}

		String columnOptions = tokensToCommandString(commandTokens).toLowerCase().substring(0, valuesIndex);
		int openBracketIndex = columnOptions.indexOf("(");

		if(openBracketIndex != -1) {
			tableName = tokensToCommandString(commandTokens).substring("INSERT INTO".length(), openBracketIndex).trim();
			int closeBracketIndex = tokensToCommandString(commandTokens).indexOf(")");
			if(closeBracketIndex == -1) {
				System.out.println("Expected ')'");
				return;
			}

			columns = tokensToCommandString(commandTokens).substring(openBracketIndex + 1, closeBracketIndex).trim();
		}

		if(tableName.equals("")) {
			tableName = tokensToCommandString(commandTokens).substring("INSERT INTO".length(), valuesIndex).trim();
		}

		String valuesList = tokensToCommandString(commandTokens).substring(valuesIndex + "values".length()).trim();
		if(!valuesList.startsWith("(")){
			System.out.println("Expected '('");
			return;
		}

		if(!valuesList.endsWith(")")){
			System.out.println("Expected ')'");
			return;
		}

		valuesList = valuesList.substring(1, valuesList.length()-1);

//		System.out.println(tableName);
//		System.out.println(valuesList);
//		System.out.println(columns);

		String[] valuesStringList = valuesList.split(",");
		for(int i=0; i<valuesStringList.length;i++){
			valuesStringList[i].replace("'","");
			valuesStringList[i] = valuesStringList[i].trim();
		}

		try{
			RandomAccessFile tableFile = new RandomAccessFile("data/user_data/" + tableName + ".tbl", "rw");
			long fileSize = tableFile.length();
			tableFile.seek(fileSize);
			//byte[] bytes = new byte[20];
			for(int i=0; i<valuesStringList.length;i++){
				tableFile.writeBytes("#");
				tableFile.writeBytes(valuesStringList[i]);
				tableFile.writeBytes("#");
				for(int j=0;j<18-valuesStringList[i].length();j++){
					tableFile.writeByte(20);
				}
			}


			tableFile.close();

			RandomAccessFile tableFile2 = new RandomAccessFile("data/user_data/" + tableName + ".tbl", "rw");
			long  Filesize=tableFile2.length();
			tableFile2.seek(0);
			//long num_of_records= Filesize/row_count;
			String str = tableFile2.readLine();
			//System.out.println(str);
			System.out.println("");
			System.out.println("1 row has been inserted in "+ tableName.toUpperCase()+ " table.");
			tableFile2.close();

		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public static void parseDelete(ArrayList<String> commandTokens) {
		System.out.println("Command: " + tokensToCommandString(commandTokens));
		System.out.println("Stub: This is the deleteRecord method");
		/* TODO: Your code goes here */
		String tableName = "";
		String condition = "";
		int index = tokensToCommandString(commandTokens).toLowerCase().indexOf("where");
		if (index == -1) {
			tableName = tokensToCommandString(commandTokens).substring("DELETE FROM".length()).trim();
		}

		if (tableName.equals("")) {
			tableName = tokensToCommandString(commandTokens).substring("DELETE FROM".length(), index).trim();
		}

		condition = tokensToCommandString(commandTokens).substring(index + "where".length());
		tableName = tableName.trim();
		condition = condition.trim();

//		System.out.println(tableName);
//		System.out.println(condition);
		String[] conditionArr = condition.split("=");
		String whereConditionColumn = conditionArr[0];
		conditionArr[1] = conditionArr[1].trim();
		conditionArr[1] = conditionArr[1].substring(1, conditionArr[1].length() - 1);
		String valueColumn = conditionArr[1];

		whereConditionColumn = whereConditionColumn.trim();
		valueColumn = valueColumn.trim();
//		System.out.println(whereConditionColumn);
//		System.out.println(valueColumn);
		try {
			RandomAccessFile tableFile2 = new RandomAccessFile("data/user_data/" + tableName + ".tbl", "rw");
			tableFile2.seek(0);
			//System.out.println(tableFile2.readLine());
			tableFile2.seek(0);
			int col_count = Integer.parseInt(String.valueOf((char) tableFile2.readByte()));
			//System.out.println("total columns are : " + col_count);
			tableFile2.seek(1);

			String recordsString = tableFile2.readLine();
			//System.out.println(recordsString);
			String[] records_data = recordsString.split("#");

			int col_id = -1;
			int count = 0;
			for (int i = 1; i < col_count * 4; i = i + 4) {
				count++;
				if (records_data[i].contentEquals(whereConditionColumn)) {
					col_id = count;
					//System.out.println("index of column : " + col_id);
					break;
				}
				//System.out.print(records_data[i]);
			}
			for (int i = 0; i < records_data.length; i++) {
				//System.out.print(records_data[i] + " ");
			}
			//System.out.println("");
			int index_to_be_deleted = -1;
			int record_index_to_be_deleted = -1;
			for (int i = col_count * 4 + 1 + ((col_id - 1) * 2); i < records_data.length; i = i + col_count * 2) {
				if (records_data[i].contentEquals(valueColumn)) {
					index_to_be_deleted = i;
//					System.out.println("index to be deleted :" + index_to_be_deleted);
//					System.out.println("Record index to be deleted :" + (index_to_be_deleted - (1 + col_count*4))/(col_count*2));
					record_index_to_be_deleted = (index_to_be_deleted - (1 + col_count*4))/(col_count*2);
					break;
				}
			}
			tableFile2.close();

			RandomAccessFile tableFile3 = new RandomAccessFile("data/user_data/" + tableName + ".tbl", "rw");
			tableFile3.seek(0);
			tableFile3.seek((col_count*2*20) + 1 + (col_count*20*record_index_to_be_deleted));
			for(int i=0 ; i< col_count; i++){
				tableFile3.writeBytes("#");
				for(int j=0;j<18;j++){
					tableFile3.writeByte(20);
				}
				tableFile3.writeBytes("#");
//				if(String.valueOf((char)tableFile3.readByte()).contentEquals("#")){
//					continue;
//				}

			}
			String test  = tableFile3.readLine();
			//System.out.println(test);
			System.out.println("");
			System.out.println("Row has been deleted successfully. 1 row affected.");
			tableFile3.close();





		} catch (Exception e){
			e.printStackTrace();
		}
	}


	/**
	 *  Stub method for dropping tables
	 */
	public static void dropTable(ArrayList<String> commandTokens) {
		System.out.println("Command: " + tokensToCommandString(commandTokens));
		System.out.println("Stub: This is the dropTable method.");
		String tableName = tokensToCommandString(commandTokens).substring("DROP TABLE".length());
		tableName = tableName.trim();
		//System.out.println(tableName);
		File myObj = new File("data/user_data/" + tableName + ".tbl");
		if (myObj.delete()) {
			System.out.println(tableName.toUpperCase()+ " is deleted from the database.");
		}
	}

	/**
	 *  Stub method for executing queries
	 */
	public static void parseQuery(ArrayList<String> commandTokens) {
		System.out.println("Command: " + tokensToCommandString(commandTokens));
		System.out.println("Stub: This is the parseQuery method");
		int index = tokensToCommandString(commandTokens).toLowerCase().indexOf("from");
		if(index == -1) {
			System.out.println("Expected FROM keyword");
			return;
		}
		String attributeListString = tokensToCommandString(commandTokens).toLowerCase().substring("SELECT".length(), index).trim();
		String restUserQuery = tokensToCommandString(commandTokens).toLowerCase().substring(index + "from".length());
		String[] attributes;
		String tableName;
		index = restUserQuery.toLowerCase().indexOf("where");
		if(index == -1) {
			tableName = restUserQuery.trim();
			//System.out.println(tableName);
			attributes = attributeListString.split(",");
			for(int i=0; i<attributes.length;i++){
				//System.out.println(attributes[i]);
			}
		} else {
			tableName = restUserQuery.substring(0, index);
			//System.out.println(tableName);
			attributes = attributeListString.split(",");
			for (int i = 0; i < attributes.length; i++) {
				attributes[i] = attributes[i].trim();
				//System.out.println(attributes[i]);
			}
			String conditions = restUserQuery.substring(index + "where".length());
			//System.out.println(conditions);
		}


		boolean isSelectAll = false;
		ArrayList<String> columns = new ArrayList<>();
		for(String attribute : attributes){
			columns.add(attribute.trim());
		}

		if(columns.size() == 1 && columns.get(0).equals("*")) {
			isSelectAll = true;
			columns = null;
		}

		try{
			RandomAccessFile tableFile2 = new RandomAccessFile("data/user_data/" + tableName + ".tbl", "rw");
			tableFile2.seek(0);
			//System.out.println(tableFile2.readLine());
			tableFile2.seek(0);
			int col_count = Integer.parseInt(String.valueOf((char)tableFile2.readByte()));
			//System.out.println("total columns are : " + col_count);
			tableFile2.seek(1);

			String recordsString = tableFile2.readLine();
			//System.out.println(recordsString);
			String[] records_data = recordsString.split("#");
			for(int i=1;i<col_count*4;i=i+4){
				System.out.print(records_data[i]);
				for(int k=0;k<20-records_data[i].length();k++){
						System.out.print(" ");
					}
			}
			System.out.println("");
			for(int k=0;k<20*col_count;k++){
				System.out.print("-");
			}
			System.out.println("");
			for(int i=col_count*4+1;i<records_data.length;){
				boolean is20 = false;
				for(int j=0; j<col_count;j++,i=i+2){
					if(records_data[i].contains("\u0014")){
						is20 = true;
						continue;
					}
					System.out.print(records_data[i]);
					for(int k=0;k<20-records_data[i].length();k++){
						System.out.print(" ");
					}
				}
				if(!is20)
				System.out.println("");
			}
			tableFile2.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void parseUpdate(ArrayList<String> commandTokens) {
		System.out.println("Command: " + tokensToCommandString(commandTokens));
		System.out.println("Stub: This is the parseUpdate method");
	}

	public static String tokensToCommandString (ArrayList<String> commandTokens) {
		String commandString = "";
		for(String token : commandTokens)
			commandString = commandString + token + " ";
		return commandString;
	}

	public static ArrayList<String> commandStringToTokenList (String command) {
		command.replace("\n", " ");
		command.replace("\r", " ");
		command.replace(",", " , ");
		command.replace("\\(", " ( ");
		command.replace("\\)", " ) ");
		ArrayList<String> tokenizedCommand = new ArrayList<String>(Arrays.asList(command.split(" ")));
		return tokenizedCommand;
	}

	/**
	 *  Help: Display supported commands
	 */
	public static void help() {
		out.println(Utils.printSeparator("*",80));
		out.println("SUPPORTED COMMANDS\n");
		out.println("All commands below are case insensitive\n");
		out.println("SHOW TABLES;");
		out.println("\tDisplay the names of all tables.\n");
		out.println("SELECT ⟨column_list⟩ FROM table_name [WHERE condition];\n");
		out.println("\tDisplay table records whose optional condition");
		out.println("\tis <column_name> = <value>.\n");
		out.println("INSERT INTO (column1, column2, ...) table_name VALUES (value1, value2, ...);\n");
		out.println("\tInsert new record into the table.");
		out.println("UPDATE <table_name> SET <column_name> = <value> [WHERE <condition>];");
		out.println("\tModify records data whose optional <condition> is\n");
		out.println("DROP TABLE table_name;");
		out.println("\tRemove table data (i.e. all records) and its schema.\n");
		out.println("VERSION;");
		out.println("\tDisplay the program version.\n");
		out.println("HELP;");
		out.println("\tDisplay this help information.\n");
		out.println("EXIT;");
		out.println("\tExit the program.\n");
		out.println(Utils.printSeparator("*",80));
	}

}
