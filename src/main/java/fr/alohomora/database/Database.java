package fr.alohomora.database;

import fr.alohomora.Configuration;
import fr.alohomora.model.Data;
import fr.alohomora.model.Element;
import fr.alohomora.model.Group;

import java.io.*;
import java.sql.*;
import java.util.Scanner;

/**
 * Alohomora Password Manager
 * Copyright (C) 2018 Team Alohomora
 * Léo BERGEROT, Sylvain COMBRAQUE, Sarah LAMOTTE, Nathan JANCZEWSKI
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **/
public class Database {

	private static Database _INSTANCE;

	private Connection con;

	private Database() {
		try {
			this.con = DriverManager.getConnection("jdbc:sqlite:" + Configuration.DB_FILE.getAbsolutePath());
			if (this.checkTable())
				this.loadSqlScript();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Statement createQuery() throws SQLException {
		return this.con.createStatement();
	}

	public void close() {
		if (this.con != null) {
			try {
				this.con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * check if the instance exist and return the unique instance of connexion
	 * @return
	 */
	public static Database getInstance() {
		if (Database._INSTANCE == null)
			Database._INSTANCE = new Database();
		return Database._INSTANCE;
	}

	/**
	 * load the the script's table of the database from the ressources folders
	 */
	public void loadSqlScript() {
		StringBuilder sqlScript = new StringBuilder();
		Statement st = null;
		try {
			st = this.con.createStatement();
			ClassLoader classLoader = getClass().getClassLoader();
			File filesql = new File(classLoader.getResource("assets/sql/db_generation.sql").getFile());
			Scanner scanner = new Scanner(filesql);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				sqlScript.append(line).append("\n");
			}
			scanner.close();
			for (String e : sqlScript.toString().split(";")) {
				st.execute(e);
			}

			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * check if the table exist true if the table not exist
	 * @return boolean
	 */
	public boolean checkTable() {
		int nbTable = 0;
		try {
			Statement st = this.con.createStatement();
			ResultSet rs = st.executeQuery("SELECT COUNT(name) FROM sqlite_master;");
			while (rs.next()) {
				nbTable = rs.getInt("COUNT(name)");
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nbTable == 0 ? true : false;
	}

	/**
	 * Insert token in DB
	 * @param username
	 * @param token
	 * @return
	 */
	public boolean insertConfig(String username, String token, boolean portable) {
		boolean res = false;
		try {
			PreparedStatement prepStmt = this.con.prepareStatement("INSERT INTO config (username, token, portable) VALUES ( ?, ?, ?)");
			prepStmt.setString(1, username);
			prepStmt.setString(2, token);
			prepStmt.setBoolean(3, portable);
			res = prepStmt.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * get Token in DB
	 * @return token
	 */
	public String getToken() {
		String res = null;
		try {
			Statement st = this.con.createStatement();
			ResultSet rs = st.executeQuery("SELECT token FROM config WHERE idConfig = 1");
			while (rs.next()) {
				res = rs.getString("token");
			}
			st.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * check if the element exist with his id, and return true or false
	 * @param id
	 * @return
	 */
	public boolean checkElementExist(int id) {
		boolean res = false;
		try {
			PreparedStatement prepStmt = this.con.prepareStatement("SELECT idElement FROM element WHERE idElement = ? ");
			prepStmt.setInt(1, id);
			ResultSet rs = prepStmt.executeQuery();
			res = rs.next();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * obtain the last request id and auto increment them
	 * @return
	 */
	public int getRequestId(){
		Integer res = null;
		try {

			ResultSet rs = this.createQuery().executeQuery("SELECT requestID  FROM config");
			while (rs.next()) {
				res = rs.getInt("requestID");
			}
			PreparedStatement prepStat = this.con.prepareStatement("UPDATE config SET requestID = ? ");
			prepStat.setInt(1,res+1);
			prepStat.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * add element
	 * @param idElement
	 * @param parent_grp
	 * @param content
	 * @return
	 */
	public boolean insertElement(int idElement, int parent_grp, String content){
		boolean res  = false;
		try{
			PreparedStatement prepStat = this.con.prepareStatement("INSERT INTO element (idElement, content, idGroupe) VALUES (?,?,?)");
			prepStat.setInt(1, idElement);
			prepStat.setString(2, content);
			prepStat.setInt(3, parent_grp);
			res = prepStat.execute();
		}catch (Exception e){
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Update Elment
	 * @param idElement
	 * @param parent_grp
	 * @param content
	 * @return
	 */
	public boolean updateElement(int idElement, int parent_grp, String content){
		boolean res = false;
		try{
			PreparedStatement prepStat = this.con.prepareStatement("UPDATE element SET idGroupe = ?, content = ? WHERE idElement = ? ");
			prepStat.setInt(3, idElement);
			prepStat.setInt(1, parent_grp);
			prepStat.setString(2, content);
			res = prepStat.execute();
		}catch (Exception e){
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * remove Element
	 * @param idElement
	 * @return
	 */
	public boolean removeElement(int idElement){
		boolean res = false;
		try{
			PreparedStatement prepStat = this.con.prepareStatement("DELETE FROM element WHERE idElement = ?");
			prepStat.setInt(1, idElement);
			res = prepStat.execute();
		}catch (Exception e){
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * check if the group exist with his id, and return true or false
	 * @param id
	 * @return
	 */
	public boolean checkGroupExist(int id) {
		boolean res = false;
		try {
			PreparedStatement prepStmt = this.con.prepareStatement("SELECT idGroupe FROM directory WHERE idGroupe = ? ");
			prepStmt.setInt(1, id);
			ResultSet rs = prepStmt.executeQuery();
			res = rs.next();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * add group
	 * @param idGroupe
	 * @param parent_grp
	 * @param content
	 * @return
	 */
	public boolean insertGroup(int idGroupe, int parent_grp, String content){
		boolean res  = false;
		try{
			PreparedStatement prepStat = this.con.prepareStatement("INSERT INTO directory (idGroupe, content, parent_grp) VALUES (?,?,?)");
			prepStat.setInt(1, idGroupe);
			prepStat.setString(2, content);
			prepStat.setInt(3, parent_grp);
			res = prepStat.execute();
		}catch (Exception e){
			e.printStackTrace();
		}
		return res;
	}

	public String getUserName(String token){
		String res = null;

		try{
			PreparedStatement preparedStatement = this.con.prepareStatement("SELECT username FROM config WHERE token = ?");
			preparedStatement.setString(1,token);
			ResultSet rs  = preparedStatement.executeQuery();
			while (rs.next()) {

				res =  rs.getString("username");

			}

	} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

		/**
	 * getGroup from database
	 */
	public Data getData(){
		int id;
		String content;
		int parent_grp;
		Data data = new Data();
		try{
			ResultSet rs = this.createQuery().executeQuery("SELECT idGroupe, content, parent_grp FROM directory");
			while (rs.next()) {

				content =  rs.getString("content");
				id = rs.getInt("idGroupe");
				parent_grp = rs.getInt("parent_grp");
				data.addGroups(new Group(id,parent_grp,content));
			}

			rs = this.createQuery().executeQuery("SELECT idElement, content, idGroupe FROM element");
			while (rs.next()){
				content = rs.getString("content");
				id = rs.getInt("idElement");
				parent_grp = rs.getInt("idGroupe");
				data.addElement(new Element(id, parent_grp, content));
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		Group g = new Group(1, -1,"","");
		data.getGroups().add(g);

		return data;
	}



 }
