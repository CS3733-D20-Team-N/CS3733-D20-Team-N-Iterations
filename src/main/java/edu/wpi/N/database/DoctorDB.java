package edu.wpi.N.database;

import edu.wpi.N.entities.DbNode;
import edu.wpi.N.entities.employees.Doctor;
import java.sql.*;
import java.util.LinkedList;

public class DoctorDB {
  private static Connection con = MapDB.getCon();

  /**
   * Gets a list of doctors associated with a given node
   *
   * @param nodeID The id of the given node
   * @return A LinkedList of all of the doctors associated with the given node
   * @throws DBException on error
   */
  public static LinkedList<Doctor> getDoctorsByLocation(String nodeID) throws DBException {
    try {
      String query =
          "SELECT doctors.doctorID FROM doctors, location WHERE location.nodeID = ? AND doctors.doctorID = location.doctor";
      PreparedStatement st = con.prepareStatement(query);
      st.setString(1, nodeID);
      ResultSet rs = st.executeQuery();

      LinkedList<Doctor> result = new LinkedList<>();
      while (rs.next()) {
        result.add(getDoctor(rs.getInt("doctorID")));
      }

      return result;
    } catch (SQLException e) {
      throw new DBException("Unknown error: getDoctorsByDept", e);
    }
  }

  /**
   * Gets the doctor with the specified ID
   *
   * @param doctorID The exact name of the doctor
   * @return The doctor asked for
   * @throws DBException On error or doctor not found
   */
  public static Doctor getDoctor(int doctorID) throws DBException {
    try {
      String query = "SELECT nodeID FROM location WHERE doctor = ? ORDER BY priority";
      PreparedStatement state = con.prepareStatement(query);
      state.setInt(1, doctorID);
      ResultSet rs = state.executeQuery();
      LinkedList<DbNode> offices = new LinkedList<>();
      while (rs.next()) {
        offices.add(MapDB.getNode(rs.getString("nodeID")));
      }

      query = "SELECT name FROM employees WHERE employeeID = ?";
      state = con.prepareStatement(query);
      state.setInt(1, doctorID);
      rs = state.executeQuery();
      String name;

      if (rs.next()) {
        name = rs.getString("name");
      } else {
        throw new DBException("getDoctor: Doctor not found");
      }

      query = "SELECT doctorID, field, username FROM doctors WHERE doctorID = ?";
      state = con.prepareStatement(query);
      state.setInt(1, doctorID);
      rs = state.executeQuery();
      if (rs.next()) {
        return new Doctor(
            rs.getInt("doctorID"), name, rs.getString("field"), rs.getString("username"), offices);
      } else {
        throw new DBException("getDoctor: Doctor not found");
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getDoctor", e);
    }
  }

  /**
   * Gets a doctor based on username. Works well since username is a unique key in Doctor
   *
   * @param username The username of the doctor you want to get
   * @return The doctor with the specified username
   * @throws DBException When there is no doctor with that username or on error
   */
  public static Doctor getDoctor(String username) throws DBException {
    String query = "SELECT doctorID FROM doctors WHERE username = ?";
    try {
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setString(1, username);
      ResultSet rs = stmt.executeQuery();
      rs.next();
      return getDoctor(rs.getInt("doctorID"));
    } catch (SQLException e) {
      if (e.getSQLState().equals("24000")) {
        throw new DBException("There is no doctor associated with that username!");
      } else {
        e.printStackTrace();
        throw new DBException("Unknown error: getDoctor username: " + username, e);
      }
    }
  }

  /**
   * Adds a doctor to the database
   *
   * @param name The doctor's name
   * @param field The field in which they work
   * @param offices A linked list of all of their office in order of priority
   * @return the id of the doctor created
   * @throws DBException If there is an error in adding a doctor
   */
  public static int addDoctor(
      String name, String field, String username, String password, LinkedList<DbNode> offices)
      throws DBException {
    try {
      con.setAutoCommit(false);

      LoginDB.createDoctorLogin(username, password);
      String query = "INSERT INTO employees (name, serviceType) VALUES (?, 'Medicine')";
      PreparedStatement state = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      state.setString(1, name);
      state.executeUpdate();

      ResultSet rs = state.getGeneratedKeys();
      rs.next();
      int id = rs.getInt("1");

      query = "INSERT INTO doctors (doctorID, field, username) VALUES(?, ?, ?)";
      state = con.prepareStatement(query);
      state.setInt(1, id);
      state.setString(2, field);
      state.setString(3, username);
      state.executeUpdate();

      if (offices == null) {
        con.commit();
        con.setAutoCommit(true);
        return id;
      }

      for (DbNode office : offices) {
        query = "INSERT INTO location (doctor, nodeID) VALUES (?, ?)";
        state = con.prepareStatement(query);
        state.setInt(1, id);
        state.setString(2, office.getNodeID());
        state.executeUpdate();
      }

      con.commit();
      con.setAutoCommit(true);
      return id;
    } catch (SQLException e) {
      try {
        con.rollback();
        con.setAutoCommit(true);
      } catch (SQLException ex) {
        ex.printStackTrace();
        throw new DBException("Unknown error: addDoctor", e);
      }
      e.printStackTrace();
      throw new DBException("Unknown error: addDoctor", e);
    } catch (DBException e) {
      try {
        con.rollback();
        con.setAutoCommit(true);
      } catch (SQLException ex) {
        ex.printStackTrace();
        throw new DBException("Unknown error: addDoctor", e);
      }
      throw e;
    }
  }

  // use removeEmployee to delete a doctor

  /**
   * Adds an office to the specified doctor's list of offices
   *
   * @param doctorID The doctor you want to add an office for
   * @param office The node of the doctor's new office
   * @throws DBException On error
   */
  public static void addOffice(int doctorID, DbNode office) throws DBException {
    try {
      String query = "INSERT INTO location (doctor, nodeID) VALUES (?, ?)";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setInt(1, doctorID);
      stmt.setString(2, office.getNodeID());
      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error, addOffice", e);
    }
  }

  /**
   * Removes an office from a doctor's list of offices
   *
   * @param doctorID The doctor from which to remove the office
   * @param office the office to remove from the doctor's list of offices
   * @return True if successful, false otherwise
   * @throws DBException On error
   */
  public static boolean removeOffice(int doctorID, DbNode office) throws DBException {
    try {
      String query = "DELETE FROM location WHERE doctor = ? AND nodeID = ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setInt(1, doctorID);
      stmt.setString(2, office.getNodeID());
      return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: removeOffice", e);
    }
  }

  /**
   * Returns a linked list of all doctors in the database.
   *
   * @return A linked list containing all the doctors in the database
   * @throws DBException on error
   */
  public static LinkedList<Doctor> getDoctors() throws DBException {
    try {
      LinkedList<Doctor> docs = new LinkedList<>();
      String query = "SELECT doctorID FROM doctors";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        docs.add(getDoctor(rs.getInt("doctorID")));
      }
      return docs;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: searchDoctors", e);
    }
  }

  /**
   * Returns all doctors where the name contains the passed-in value as a substring (case
   * insensitive).
   *
   * @param name A substring of the doctor that you're looking for
   * @return A linked list of all doctors with a name with the passed in value as a substring
   * @throws DBException on error
   */
  public static LinkedList<Doctor> searchDoctors(String name) throws DBException {
    try {
      LinkedList<Doctor> docs = new LinkedList<>();
      String query =
          "SELECT doctorID FROM doctors, (SELECT employeeID, name FROM employees) as employees WHERE doctorID = employeeID AND UPPER(name) LIKE ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setString(1, "%" + name.toUpperCase() + "%");
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        docs.add(getDoctor(rs.getInt("doctorID")));
      }
      return docs;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: searchDoctors", e);
    }
  }
}
