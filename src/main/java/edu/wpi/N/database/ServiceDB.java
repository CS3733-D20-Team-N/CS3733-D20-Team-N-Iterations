package edu.wpi.N.database;

import edu.wpi.N.entities.*;
import edu.wpi.N.entities.employees.*;
import edu.wpi.N.entities.request.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.regex.Pattern;

public class ServiceDB {
  private static Connection con = MapDB.getCon();

  // Noah
  /**
   * Returns the employee specified by the given ID
   *
   * @param id The employee's ID
   * @return an employee entity representing that employee
   * @throws DBException If an error occurs
   */
  public static Employee getEmployee(int id) throws DBException {
    try {
      if (id <= 0) return null; // handle case of unassigned employee without printing anything
      String query = "SELECT * FROM employees WHERE employeeID = ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      rs.next();
      String sType = rs.getString("serviceType");
      String name = rs.getString("name");
      switch (sType) {
        case "Translator":
          query = "SELECT language FROM language WHERE t_EmployeeID = ?";
          stmt = con.prepareStatement(query);
          stmt.setInt(1, id);
          rs = stmt.executeQuery();
          LinkedList<String> languages = new LinkedList<>();
          while (rs.next()) {
            languages.add(rs.getString("language"));
          }
          return new Translator(id, name, languages);
        case "Laundry":
          return new Laundry(id, name);
        case "Emotional Support":
          return new EmotionalSupporter(id, name);
        case "Medicine":
          return DoctorDB.getDoctor(id);
        case "Sanitation":
          return new Sanitation(id, name);
        case "Wheelchair":
          return new WheelchairEmployee(id, name);
        case "IT":
          return new IT(id, name);
        case "Flower":
          return new FlowerDeliverer(id, name);
        case "Internal Transportation":
          return new InternalTransportationEmployee(id, name);
        case "Security":
          return new SecurityOfficer(id, name);
        default:
          throw new DBException(
              "Invalid employee in table employees! ID: " + id + "Name: " + rs.getString("name"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getEmployee. ID: " + id, e);
    }
  }

  // Chris
  /**
   * Returns a list of all employees in the database
   *
   * @return a linked list of all employees in the database
   * @throws DBException If an error occurs
   */
  // of your type
  public static LinkedList<Employee> getEmployees() throws DBException {
    LinkedList<Employee> allEmployee = new LinkedList<>();
    allEmployee.addAll(getTranslators());
    allEmployee.addAll(getLaundrys());
    allEmployee.addAll(getEmotionalSupporters());
    allEmployee.addAll(DoctorDB.getDoctors());
    allEmployee.addAll(getWheelchairEmployees());
    allEmployee.addAll(getITs());
    allEmployee.addAll(getFlowerDeliverers());
    allEmployee.addAll(getSanitationEmp());
    allEmployee.addAll(getInternalTransportationEmployees());
    allEmployee.addAll(getSecurityOfficers());
    return allEmployee;
  }

  // Nick
  /**
   * Gets all services in the database
   *
   * @return a linked list of all services in the database
   * @throws DBException If an error occurs
   */
  public static LinkedList<Service> getServices() throws DBException {
    LinkedList<Service> ret = new LinkedList<>();

    try {
      String query = "SELECT * FROM service";
      PreparedStatement st = con.prepareStatement(query);
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        ret.add(
            new Service(
                rs.getString("timeStart"),
                rs.getString("timeEnd"),
                rs.getString("serviceType"),
                rs.getString("description")));
      }
      return ret;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getServices", e);
    }
  }

  private static String createFlowerString(ResultSet rs) throws SQLException {
    String flowers = "";
    String name;
    int amount;
    while (rs.next()) {
      name = rs.getString("flowerName");
      amount = rs.getInt("flowerCount");
      if (amount > 1) {
        flowers += "" + amount + " " + name + "s, ";
      } else {
        flowers += "" + amount + " " + name + ", ";
      }
    }
    if (flowers.length() > 0) flowers = flowers.substring(0, flowers.length() - 2);
    return flowers;
  }

  /**
   * Gets the request from the database with the given id
   *
   * @param id the id of the request to retrieve
   * @return a Request containing information about the request requested
   * @throws DBException If an error occurs
   */
  public static Request getRequest(int id) throws DBException {
    try {
      String query = "SELECT * FROM request WHERE requestID = ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();
      rs.next();
      int rid = rs.getInt("requestID");
      int empId = rs.getInt("assigned_eID");
      String reqNotes = rs.getString("reqNotes");
      String compNotes = rs.getString("compNotes");
      String nodeID = rs.getString("nodeID");
      GregorianCalendar timeReq = getJavatime(rs.getTimestamp("timeRequested"));
      GregorianCalendar timeComp = getJavatime(rs.getTimestamp("timeCompleted"));
      String status = rs.getString("status");
      String sType = rs.getString("serviceType");
      if (sType.equals("Laundry")) {
        return new LaundryRequest(
            rid, empId, reqNotes, compNotes, nodeID, timeReq, timeComp, status);
      } else if (sType.equals("Translator")) {
        query = "SELECT language FROM trequest WHERE requestID = ?";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        rs = stmt.executeQuery();
        rs.next();
        return new TranslatorRequest(
            rid,
            empId,
            reqNotes,
            compNotes,
            nodeID,
            timeReq,
            timeComp,
            status,
            rs.getString("language"));
      } else if (sType.equals("Wheelchair")) {
        query = "SELECT needsAssistance FROM wrequest WHERE requestID = ?";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        rs = stmt.executeQuery();
        rs.next();
        return new WheelchairRequest(
            rid,
            empId,
            reqNotes,
            compNotes,
            nodeID,
            timeReq,
            timeComp,
            status,
            rs.getString("needsAssistance"));
      } else if (sType.equals("Emotional Support")) {
        query = "SELECT supportType FROM erequest WHERE requestID = ?";

        stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        rs = stmt.executeQuery();
        rs.next();
        return new EmotionalRequest(
            rid,
            empId,
            reqNotes,
            compNotes,
            nodeID,
            timeReq,
            timeComp,
            status,
            rs.getString("supportType"));
      } else if (rs.getString("serviceType").equals("Medicine")) {
        query =
            "SELECT medicineName, dosage, units, patient FROM medicineRequests WHERE requestID = ?";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        rs = stmt.executeQuery();
        rs.next();
        return new MedicineRequest(
            rid,
            empId,
            reqNotes,
            compNotes,
            nodeID,
            timeReq,
            timeComp,
            status,
            rs.getString("medicineName"),
            rs.getDouble("dosage"),
            rs.getString("units"),
            rs.getString("patient"));
      } else if (sType.equals("Sanitation")) {
        query = "SELECT size, sanitationType, danger FROM SANITATIONREQUESTS WHERE requestID = ?";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        rs = stmt.executeQuery();
        rs.next();
        return new SanitationRequest(
            rid,
            empId,
            reqNotes,
            compNotes,
            nodeID,
            timeReq,
            timeComp,
            status,
            rs.getString("sanitationType"),
            rs.getString("size"),
            rs.getString("danger"));
      } else if (sType.equals("IT")) {
        query = "SELECT device, problem FROM ITrequest WHERE requestID = ?";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        rs = stmt.executeQuery();
        rs.next();
        return new ITRequest(
            rid,
            empId,
            reqNotes,
            compNotes,
            nodeID,
            timeReq,
            timeComp,
            status,
            rs.getString("device"),
            rs.getString("problem"));
      } else if (sType.equals("Flower")) {
        query =
            "SELECT requestID, patientName, visitorName, creditNum FROM flowerRequest WHERE requestID = ?";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        rs = stmt.executeQuery();
        rs.next();
        String a = rs.getString("patientName");
        String b = rs.getString("visitorName");
        String c = rs.getString("creditNum");
        query =
            "SELECT flower.flowerName, count(flower.flowerName) AS flowerCount FROM flower, flowertoflower "
                + "WHERE flower.flowerName = flowertoflower.flowerName AND flowertoflower.requestID = ? "
                + "GROUP BY flower.flowerName";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, rs.getInt("requestID"));
        rs = stmt.executeQuery();
        String flowers = createFlowerString(rs);
        return new FlowerRequest(
            rid, empId, reqNotes, compNotes, nodeID, timeReq, timeComp, status, a, b, c, flowers);
      } else if (sType.equals("Internal Transportation")) {
        query =
            "SELECT transportationType, scheduledTransportTime, destinationLocation FROM internalTransportationRequest "
                + "WHERE requestID = ?";

        stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        rs = stmt.executeQuery();
        rs.next();
        return new InternalTransportationRequest(
            rid,
            empId,
            reqNotes,
            compNotes,
            nodeID,
            timeReq,
            timeComp,
            status,
            rs.getString("transportationType"),
            rs.getTimestamp("scheduledTransportTime").toString().substring(11, 16),
            rs.getString("destinationLocation"));
      } else if (sType.equals("Security")) {
        query = "SELECT isEmergency FROM secrequest WHERE requestID = ?";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, id);
        rs = stmt.executeQuery();
        rs.next();
        return new SecurityRequest(
            rid,
            empId,
            reqNotes,
            compNotes,
            nodeID,
            timeReq,
            timeComp,
            status,
            rs.getString("isEmergency"));

      } else throw new DBException("Invalid request! ID = " + id);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getRequest", e);
    }
  }

  // Noah
  /**
   * Gets all the requests in the database
   *
   * @return a linked list of all service requests in the database
   * @throws DBException If an error occurs
   */
  public static LinkedList<Request> getRequests() throws DBException {
    try {
      LinkedList<Request> requests = new LinkedList<>();
      String query = "SELECT * FROM request WHERE serviceType = 'Laundry'";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        requests.add(
            new LaundryRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status")));
      }
      query = "SELECT * from request, trequest WHERE request.requestID = trequest.requestID";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        requests.add(
            new TranslatorRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("language")));
      }
      query = "SELECT * from request, erequest WHERE request.requestID = erequest.requestID";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        requests.add(
            new EmotionalRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("supportType")));
      }
      query =
          "SELECT * from request, medicineRequests WHERE request.requestID = medicineRequests.requestID";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        requests.add(
            new MedicineRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("medicineName"),
                rs.getDouble("dosage"),
                rs.getString("units"),
                rs.getString("patient")));
      }
      query =
          "SELECT * FROM request, sanitationRequests WHERE request.requestID = sanitationRequests.requestID";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        requests.add(
            new SanitationRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("sanitationType"),
                rs.getString("size"),
                rs.getString("danger")));
      }
      query = "SELECT * from request, wrequest WHERE request.requestID = wrequest.requestID";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        requests.add(
            new WheelchairRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("needsAssistance")));
      }
      query = "SELECT * from request, ITrequest WHERE request.requestID = ITrequest.requestID";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        requests.add(
            new ITRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("device"),
                rs.getString("problem")));
      }
      query =
          "SELECT * FROM request, flowerRequest WHERE request.requestID = flowerRequest.requestID";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        query =
            "SELECT flower.flowerName, count(flower.flowerName) AS flowerCount FROM flower, flowertoflower "
                + "WHERE flower.flowerName = flowertoflower.flowerName AND flowertoflower.requestID = ? "
                + "GROUP BY flower.flowerName";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, rs.getInt("requestID"));
        ResultSet rx = stmt.executeQuery();
        String flowers = createFlowerString(rx);
        requests.add(
            new FlowerRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("patientName"),
                rs.getString("visitorName"),
                rs.getString("creditNum"),
                flowers));
      }
      query =
          "SELECT * from request, internalTransportationRequest WHERE request.requestID = internalTransportationRequest.requestID";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        requests.add(
            new InternalTransportationRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("transportationType"),
                rs.getTimestamp("scheduledTransportTime").toString().substring(11, 16),
                rs.getString("destinationLocation")));
      }
      query = "SELECT * from request, secrequest WHERE request.requestID = secrequest.requestID";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        requests.add(
            new SecurityRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("isEmergency")));
      }
      return requests;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getRequests", e);
    }
  }

  // Chris
  /**
   * Gets all the open requests (not completed requests) in the database
   *
   * @return a linked list of all open service requests in the database
   * @throws DBException If an error occurs
   */
  public static LinkedList<Request> getOpenRequests() throws DBException {
    LinkedList<Request> openList = new LinkedList<>();
    try {
      String query =
          "SELECT * FROM request, trequest WHERE request.requestID = trequest.requestID AND status='OPEN'";
      PreparedStatement stmt = con.prepareStatement(query);

      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        openList.add(
            new TranslatorRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("language")));
      }
      query =
          "SELECT * FROM request, lrequest WHERE request.requestID = lrequest.requestID AND status = 'OPEN'";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        openList.add(
            new LaundryRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status")));
      }
      query =
          "SELECT * FROM request, wrequest WHERE request.requestID = wrequest.requestID AND status = 'OPEN'";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        openList.add(
            new WheelchairRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("needsAssistance")));
      }
      query =
          "SELECT * FROM request, erequest WHERE request.requestID = erequest.requestID AND status = 'OPEN'";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        openList.add(
            new EmotionalRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("supportType")));
      }
      query =
          "SELECT * from request, ITrequest WHERE request.requestID = ITrequest.requestID AND status = 'OPEN'";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        openList.add(
            new ITRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("device"),
                rs.getString("problem")));
      }
      query =
          "SELECT * from request, sanitationRequests WHERE request.requestID = sanitationRequests.requestID AND status = 'OPEN'";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        openList.add(
            new SanitationRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("sanitationType"),
                rs.getString("size"),
                rs.getString("danger")));
      }
      query =
          "SELECT * FROM request, flowerRequest WHERE request.requestID = flowerRequest.requestID AND status = 'OPEN'";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        query =
            "SELECT flower.flowerName, count(flower.flowerName) AS flowerCount FROM flower, flowertoflower "
                + "WHERE flower.flowerName = flowertoflower.flowerName AND flowertoflower.requestID = ? "
                + "GROUP BY flower.flowerName";
        stmt = con.prepareStatement(query);
        stmt.setInt(1, rs.getInt("requestID"));
        ResultSet rx = stmt.executeQuery();
        String flowers = createFlowerString(rx);
        openList.add(
            new FlowerRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("patientName"),
                rs.getString("visitorName"),
                rs.getString("creditNum"),
                flowers));
      }
      query =
          "SELECT * from request, internalTransportationRequest "
              + "WHERE request.requestID = internalTransportationRequest.requestID AND status = 'OPEN'";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        openList.add(
            new InternalTransportationRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("transportationType"),
                rs.getTimestamp("scheduledTransportTime").toString().substring(11, 16),
                rs.getString("destinationLocation")));
      }
      query =
          "SELECT * from request, secrequest WHERE request.requestID = secrequest.requestID AND status = 'OPEN'";
      stmt = con.prepareStatement(query);
      rs = stmt.executeQuery();
      while (rs.next()) {
        openList.add(
            new SecurityRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("isEmergency")));
      }
      return openList;
    } catch (SQLException ex) {
      ex.printStackTrace();
      throw new DBException("Error: getOpenRequest", ex);
    }
  }

  // Nick
  /**
   * Gets all the translators in the database
   *
   * @return a linked list of all translators in the database
   * @throws DBException If an error occurs
   */
  public static LinkedList<Translator> getTranslators() throws DBException {
    try {
      String query =
          "SELECT t_employeeID from employees, translator where employeeID = t_employeeID";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      LinkedList<Translator> translators = new LinkedList<>();
      while (rs.next()) {
        translators.add((Translator) getEmployee(rs.getInt("t_employeeID")));
      }
      return translators;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getTranslators", e);
    }
  }

  // Noah
  /**
   * Gets all the laundrys in the database
   *
   * @return a linked list of all people who can do laundry in the database
   * @throws DBException If an error occurs
   */
  public static LinkedList<Laundry> getLaundrys() throws DBException {
    try {
      String query = "SELECT l_employeeID from employees, laundry where employeeID = l_employeeID";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      LinkedList<Laundry> laundrys = new LinkedList<>();
      while (rs.next()) {
        laundrys.add((Laundry) getEmployee(rs.getInt("l_employeeID")));
      }
      return laundrys;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getLaundrys", e);
    }
  }

  /**
   * Gets a list of all internal transportation requests
   *
   * @return a LinkedList of InternalTransportationRequest
   * @throws DBException If an error occurs
   */
  public static LinkedList<InternalTransportationEmployee> getInternalTransportationEmployees()
      throws DBException {
    try {
      String query =
          "SELECT inTr_employeeID from employees, internalTransportationEmployee where employeeID = inTr_employeeID";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      LinkedList<InternalTransportationEmployee> emps = new LinkedList<>();
      while (rs.next()) {
        emps.add((InternalTransportationEmployee) getEmployee(rs.getInt("inTr_employeeID")));
      }
      return emps;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getInternalTransportationEmployees", e);
    }
  }

  /**
   * gets a list of all flower deliverers
   *
   * @return list of all flower deliverers
   * @throws DBException If an error occurs
   */
  public static LinkedList<FlowerDeliverer> getFlowerDeliverers() throws DBException {
    try {
      String query =
          "SELECT f_employeeID from employees, flowerDeliverer where employeeID = f_employeeID";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      LinkedList<FlowerDeliverer> list = new LinkedList<>();
      while (rs.next()) {
        list.add((FlowerDeliverer) getEmployee(rs.getInt("f_employeeID")));
      }
      return list;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getflowerDeliverer", e);
    }
  }

  /**
   * Gets all the emotional supporters in the database
   *
   * @return a linked list of all people who can do emotional support in the database
   * @throws DBException If an error occurs
   */
  public static LinkedList<EmotionalSupporter> getEmotionalSupporters() throws DBException {
    try {
      String query =
          "SELECT e_employeeID from employees, emotionalSupporter where employeeID = e_employeeID";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      LinkedList<EmotionalSupporter> emotionalSupporters = new LinkedList<>();
      while (rs.next()) {
        emotionalSupporters.add((EmotionalSupporter) getEmployee(rs.getInt("e_employeeID")));
      }
      return emotionalSupporters;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getEmotionalSupporters", e);
    }
  }

  /**
   * Gets all the wheelchair employees in the database
   *
   * @return LinkedList<WheelchairEmployee>, all wheelchair employees
   * @throws DBException If an error occurs
   */
  public static LinkedList<WheelchairEmployee> getWheelchairEmployees() throws DBException {
    try {
      String query =
          "SELECT w_employeeID from employees, wheelchairEmployee where employeeID = w_employeeID";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      LinkedList<WheelchairEmployee> wheelchairEmployees = new LinkedList<>();
      while (rs.next()) {
        wheelchairEmployees.add((WheelchairEmployee) getEmployee(rs.getInt("w_employeeID")));
      }
      return wheelchairEmployees;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getWheelchairEmployees", e);
    }
  }
  /**
   * Gets all the IT employees in the database
   *
   * @return a linked list of all people who can do IT in the database
   * @throws DBException If an error occurs
   */
  public static LinkedList<IT> getITs() throws DBException {
    try {
      String query = "SELECT IT_employeeID from employees, IT where employeeID = IT_employeeID";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      LinkedList<IT> ITs = new LinkedList<>();
      while (rs.next()) {
        ITs.add((IT) getEmployee(rs.getInt("IT_employeeID")));
      }
      return ITs;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getITs", e);
    }
  }

  /**
   * Gets all the security officers in the database
   *
   * @return a linked list of all security officers in the database
   * @throws DBException If an error occurs
   */
  public static LinkedList<SecurityOfficer> getSecurityOfficers() throws DBException {
    try {
      String query =
          "SELECT sec_employeeID from employees, security where employeeID = sec_employeeID";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      LinkedList<SecurityOfficer> secOffs = new LinkedList<>();
      while (rs.next()) {
        secOffs.add((SecurityOfficer) getEmployee(rs.getInt("sec_employeeID")));
      }
      return secOffs;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getSecurityOfficers", e);
    }
  }

  // Chris
  /**
   * Returns a list of all translators who speak a specified langauge
   *
   * @param lang the language that you want the translators to speak
   * @return a linked list of all the translators who speak a specified language
   * @throws DBException If an error occurs
   */
  public static LinkedList<Translator> getTransLang(String lang) throws DBException {
    try {
      String query =
          "SELECT translator.t_employeeID FROM translator, (SELECT * FROM language where language = ?) AS language"
              + " WHERE translator.t_employeeID = language.t_employeeID";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setString(1, lang);
      ResultSet rs = stmt.executeQuery();
      LinkedList<Translator> translators = new LinkedList<>();
      while (rs.next()) {
        translators.add((Translator) getEmployee(rs.getInt("t_employeeID")));
      }
      return translators;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getTransLang, lang :" + lang, e);
    }
  }

  // Nick
  /**
   * Adds a translator to the database
   *
   * @param name the translator's name
   * @param languages the languages that this translator is capable of speaking
   * @return id of created translator
   * @throws DBException If an error occurs
   */
  public static int addTranslator(String name, LinkedList<String> languages) throws DBException {
    try {
      String query = "INSERT INTO employees (name, serviceType) VALUES (?, 'Translator')";
      PreparedStatement st = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      st.setString(1, name);
      st.executeUpdate();
      ResultSet rs = st.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO translator VALUES(?)";
      st = con.prepareStatement(query);
      int id = rs.getInt("1");
      st.setInt(1, id);
      st.executeUpdate();
      if (languages == null) return id;
      for (String language : languages) {
        query = "INSERT INTO language VALUES (?, ?)";
        st = con.prepareStatement(query);
        st.setInt(1, id);
        st.setString(2, language);
        st.executeUpdate();
      }
      return id;
    } catch (SQLException e) {
      // e.printStackTrace();
      throw new DBException("Unknown error: addTranslator", e);
    }
  }

  // Noah
  /**
   * Adds a laundry employee to the database
   *
   * @param name the laundry employee's name
   * @return id of created request
   * @throws DBException If an error occurs
   */
  public static int addLaundry(String name) throws DBException {
    try {
      String query = "INSERT INTO employees (name, serviceType) VALUES (?, 'Laundry')";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, name);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO Laundry VALUES (?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: addLaundry , name = " + name, e);
    }
  }

  // Chris
  /**
   * adds a sanitation employee with the specified name
   *
   * @param name The name of the employee
   * @return the generated id
   * @throws DBException If an error occurs
   */
  public static int addSanitationEmp(String name) throws DBException {
    try {
      String query = "INSERT INTO employees (name, serviceType) VALUES (?, 'Sanitation')";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, name);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO sanitation VALUES (?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt(1);
      stmt.setInt(1, id);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown Error: addSanitationEmp");
    }
  }

  /**
   * Adds an internal transportation employee to the database
   *
   * @param name The name of the employee
   * @return The id of the newly added employee
   * @throws DBException if there was an error in adding the employee
   */
  public static int addInternalTransportationEmployee(String name) throws DBException {
    try {
      String query =
          "INSERT INTO employees (name, serviceType) VALUES (?, 'Internal Transportation')";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, name);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO INTERNALTRANSPORTATIONEMPLOYEE VALUES (?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      throw new DBException("Unknown Error: addInternalTransportationEmployee", e);
    }
  }

  /**
   * Adds a Flower Deliverer to the database
   *
   * @param name The name of the flower deliverer
   * @return the employeeID of the generated Employee
   * @throws DBException If an error occurs
   */
  public static int addFlowerDeliverer(String name) throws DBException {
    try {
      String query = "INSERT INTO employees (name, serviceType) VALUES (?, 'Flower')";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, name);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO flowerDeliverer VALUES (?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown Error: addflowerDeliverer", e);
    }
  }

  /**
   * Adds a Emotional Supporter employee to the database
   *
   * @param name the Emotional supporter employee's name
   * @return id of created request
   * @throws DBException If an error occurs
   */
  public static int addEmotionalSupporter(String name) throws DBException {
    try {
      String query = "INSERT INTO employees (name, serviceType) VALUES (?, 'Emotional Support')";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, name);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next(); // NullPointerException
      query = "INSERT INTO emotionalSupporter VALUES (?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: addEmotionalSupporter , name = " + name, e);
    }
  }

  /**
   * adds a wheelchair employee to the database
   *
   * @param name the wheelchair employee's name
   * @return the employeeID of the newly added employee
   * @throws DBException If an error occurs
   */
  public static int addWheelchairEmployee(String name) throws DBException {
    try {
      String query = "INSERT INTO employees (name, serviceType) VALUES (?, 'Wheelchair')";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, name);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next(); // NullPointerException
      query = "INSERT INTO WheelchairEmployee VALUES (?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: addWheelchairEmployee , name = " + name, e);
    }
  }
  /**
   * Adds a IT employee to the database
   *
   * @param name the IT employee's name
   * @return id of created request
   * @throws DBException If an error occurs
   */
  public static int addIT(String name) throws DBException {
    try {
      String query = "INSERT INTO employees (name, serviceType) VALUES (?, 'IT')";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, name);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next(); // NullPointerException
      query = "INSERT INTO IT VALUES (?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: addIT , name = " + name, e);
    }
  }

  // Matt
  /**
   * Adds a security officer to the database
   *
   * @param name the security officer's name
   * @return id of created request
   * @throws DBException If an error occurs
   */
  public static int addSecurityOfficer(String name) throws DBException {
    try {
      String query = "INSERT INTO employees (name, serviceType) VALUES (?, 'Security')";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, name);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next(); // NullPointerException
      query = "INSERT INTO security VALUES (?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: addSecurityOfficer , name = " + name, e);
    }
  }

  // Chris
  /**
   * Adds a request for a translator
   *
   * @param reqNotes some notes for the translator request
   * @param nodeID The ID of the node in which these services are requested
   * @param language the language that the translator is requested for
   * @return the id of the created request
   * @throws DBException If an error occurs
   */
  public static int addTransReq(String reqNotes, String nodeID, String language)
      throws DBException {
    try {
      String query =
          "INSERT INTO request (timeRequested, reqNotes, serviceType, nodeID, status) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
      stmt.setString(2, reqNotes);
      stmt.setString(3, "Translator");
      stmt.setString(4, nodeID);
      stmt.setString(5, "OPEN");
      stmt.execute();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO trequest (requestID, language) VALUES (?, ?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.setString(2, language);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Error: addTransReq", e);
    }
  }

  /**
   * Adds a request for a medicine
   *
   * @param reqNotes notes for the request
   * @param nodeID the nodeID of the location for the request
   * @param medicineName the name of the medicine to be delivered
   * @param dosage the dosage of the request
   * @param units the units for the dosage
   * @param patient The patient that the medicine should be delivered to
   * @return the id of the created request
   * @throws DBException on error
   */
  public static int addMedReq(
      String reqNotes,
      String nodeID,
      String medicineName,
      double dosage,
      String units,
      String patient)
      throws DBException {
    try {
      String query =
          "INSERT INTO request (timeRequested, reqNotes, serviceType, nodeID, status) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setTimestamp(1, new Timestamp((new Date().getTime())));
      stmt.setString(2, reqNotes);
      stmt.setString(3, "Medicine");
      stmt.setString(4, nodeID);
      stmt.setString(5, "OPEN");
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query =
          "INSERT INTO medicineRequests (requestID, medicineName, dosage, units, patient) VALUES (?, ?, ?, ?, ?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.setString(2, medicineName);
      stmt.setDouble(3, dosage);
      stmt.setString(4, units);
      stmt.setString(5, patient);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: addMedReq", e);
    }
  }

  // Nick
  /**
   * Adds a request for laundry
   *
   * @param reqNotes some notes for the laundry request
   * @param nodeID The ID of the node in which these services are requested
   * @return the id of the created request
   * @throws DBException If an error occurs
   */
  public static int addLaundReq(String reqNotes, String nodeID) throws DBException {
    try {
      String query =
          "INSERT INTO request (timeRequested, reqNotes, serviceType, nodeID, status) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
      stmt.setString(2, reqNotes);
      stmt.setString(3, "Laundry");
      stmt.setString(4, nodeID);
      stmt.setString(5, "OPEN");
      stmt.execute();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO lrequest (requestID) VALUES (?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Error: addLaundReq", e);
    }
  }

  /**
   * Adds a request for internal transportation to the database
   *
   * @param reqNotes The notes of the request
   * @param nodeID The id of the node the request should be sent to
   * @param transportationType The type of transportation requested
   * @param scheduledTransportTime The time of the transportation (24-hour, in hh:mm format)
   * @param destinationLocation The node id of the destination
   * @return The id of the newly created request
   * @throws DBException If an error occurs
   */
  public static int addInternalTransportationReq(
      String reqNotes,
      String nodeID,
      String transportationType,
      String scheduledTransportTime,
      String destinationLocation)
      throws DBException {
    try {
      String query =
          "INSERT INTO request (timeRequested, reqNotes, serviceType, nodeID, status) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
      stmt.setString(2, reqNotes);
      stmt.setString(3, "Internal Transportation");
      stmt.setString(4, nodeID);
      stmt.setString(5, "OPEN");
      stmt.execute();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query =
          "INSERT INTO INTERNALTRANSPORTATIONREQUEST (requestID, TRANSPORTATIONTYPE, SCHEDULEDTRANSPORTTIME, DESTINATIONLOCATION) VALUES (?, ?, ?, ?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.setString(2, transportationType);

      SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
      Date parsedDate = dateFormat.parse(scheduledTransportTime);
      stmt.setTimestamp(3, new Timestamp(parsedDate.getTime()));

      stmt.setString(4, destinationLocation);
      stmt.executeUpdate();
      return id;
    } catch (SQLException | ParseException e) {
      throw new DBException("Error: addInternalTransportationReq", e);
    }
  }

  /**
   * Adds a floral request to the database
   *
   * @param reqNotes The notes for the request
   * @param nodeID The node id of where the request needs to be sent to
   * @param patientName The name of the patient recieving the flowers
   * @param visitorName The name of the visitor requesting the flowers
   * @param creditNum The credit card number, needed to pay for the flowers
   * @param flowerList The list of flowers to be delivered
   * @return The id of the newly created request
   * @throws DBException If an error occurs
   */
  public static int addFlowerReq(
      String reqNotes,
      String nodeID,
      String patientName,
      String visitorName,
      String creditNum,
      LinkedList<String> flowerList)
      throws DBException {
    if (flowerList == null || flowerList.size() == 0) {
      throw new DBException("addFlowerReq: The list of flowers is either empty or null");
    }
    try {
      String query =
          "INSERT INTO request (timeRequested, reqNotes, serviceType, nodeID, status) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
      stmt.setString(2, reqNotes);
      stmt.setString(3, "Flower");
      stmt.setString(4, nodeID);
      stmt.setString(5, "OPEN");
      stmt.execute();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query =
          "INSERT INTO flowerRequest (requestID, patientName, visitorName, creditNum) VALUES (?, ?, ?, ?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.setString(2, patientName);
      stmt.setString(3, visitorName);
      stmt.setString(4, creditNum);
      stmt.executeUpdate();
      query = "INSERT INTO flowertoflower (requestID, flowerName) VALUES (?, ?)";
      stmt = con.prepareStatement(query);
      stmt.setInt(1, id);
      for (String f : flowerList) {
        stmt.setString(2, f);
        stmt.executeUpdate();
      }
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: addFlowerReq", e);
    }
  }

  /**
   * Adds a new flower to the database
   *
   * @param name Name of the new flower
   * @param price Price of the new flower in cents
   * @return The flower that was added to the database
   * @throws DBException if there was an error adding the flower
   */
  public static Flower addFlower(String name, int price) throws DBException {
    try {
      String query = "INSERT INTO flower (flowerName, price) VALUES (?, ?)";
      PreparedStatement st = con.prepareStatement(query);
      st.setString(1, name);
      st.setInt(2, price);
      if (st.executeUpdate() > 0) return new Flower(name, price);
      else throw new DBException("The flower \"" + name + "\" was not added to the database");
    } catch (SQLException e) {
      throw new DBException("Unknown error: addFlower", e);
    }
  }

  /**
   * Deletes a flower from the database
   *
   * @param name The name of the flower to be deleted
   * @return True if deletion was successful
   * @throws DBException if there was an error removing the flower
   */
  public static boolean removeFlower(String name) throws DBException {
    try {
      String query = "DELETE FROM flower WHERE flowerName = ?";
      PreparedStatement st = con.prepareStatement(query);
      st.setString(1, name);
      return st.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new DBException("Unknown error: removeFlower", e);
    }
  }

  /**
   * Gets a flower from the database
   *
   * @param name Name of the flower
   * @return a Flower object containing the name and price of the flower
   * @throws DBException if there is an error in getting the flower
   */
  public static Flower getFlower(String name) throws DBException {
    try {
      String query = "SELECT * FROM flower WHERE flowerName = ?";
      PreparedStatement st = con.prepareStatement(query);
      st.setString(1, name);
      ResultSet rs = st.executeQuery();

      if (rs.next()) {
        return new Flower(name, rs.getInt("price"));
      } else throw new DBException("Could not find \"" + name + "\" in the flower table");
    } catch (SQLException e) {
      throw new DBException("Unknown error: getFlower", e);
    }
  }

  /**
   * Gets all flowers from the database
   *
   * @return a LinkedList of Flower
   * @throws DBException when there is an error in querying the database
   */
  public static LinkedList<Flower> getFlowers() throws DBException {
    try {
      String query = "SELECT * FROM flower";
      PreparedStatement st = con.prepareStatement(query);
      ResultSet rs = st.executeQuery();

      LinkedList<Flower> flowers = new LinkedList<>();
      while (rs.next()) {
        flowers.add(new Flower(rs.getString("flowerName"), rs.getInt("price")));
      }

      return flowers;
    } catch (SQLException e) {
      throw new DBException("Unknown error: getFlowers");
    }
  }

  /**
   * Adds a request for emotional support
   *
   * @param reqNotes some notes for the emotional support request
   * @param nodeID The ID of the node in which these services are requested
   * @param supportType the type of support the emotional supporter is requested for
   * @return the id of the created request
   * @throws DBException If an error occurs
   */
  public static int addEmotSuppReq(String reqNotes, String nodeID, String supportType)
      throws DBException {
    try {
      String query =
          "INSERT INTO request (timeRequested, reqNotes, serviceType, nodeID, status) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
      stmt.setString(2, reqNotes);
      stmt.setString(3, "Emotional Support");
      stmt.setString(4, nodeID);
      stmt.setString(5, "OPEN");
      stmt.execute();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO erequest (requestID, supportType) VALUES (?, ?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.setString(2, supportType);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Error: addEmotSuppReq", e);
    }
  }

  /**
   * Creates a new wheelchair request
   *
   * @param reqNotes, notes for the wheelchair request
   * @param nodeID, String, location of wheelchair request
   * @param needsAssistance, String, whether the wheelchair requester requires assistance ("yes" or
   *     "no")
   * @return int, the requestID of the new request
   * @throws DBException If an error occurs
   */
  public static int addWheelchairRequest(String reqNotes, String nodeID, String needsAssistance)
      throws DBException {
    try {
      String query =
          "INSERT INTO request (timeRequested, reqNotes, serviceType, nodeID, status) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
      stmt.setString(2, reqNotes);
      stmt.setString(3, "Wheelchair");
      stmt.setString(4, nodeID);
      stmt.setString(5, "OPEN");
      stmt.execute();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO wrequest (requestID, needsAssistance) VALUES (?, ?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.setString(2, needsAssistance);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Error: addWheelchairRequest", e);
    }
  }
  /**
   * Adds a request for IT
   *
   * @param reqNotes some notes for the IT request
   * @param nodeID The ID of the node in which these services are requested
   * @return the id of the created request
   * @throws DBException If an error occurs
   */
  public static int addITReq(String reqNotes, String nodeID, String device, String problem)
      throws DBException {
    try {
      String query =
          "INSERT INTO request (timeRequested, reqNotes, serviceType, nodeID, status) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
      stmt.setString(2, reqNotes);
      stmt.setString(3, "IT");
      stmt.setString(4, nodeID);
      stmt.setString(5, "OPEN");
      stmt.execute();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO ITrequest (requestID, device, problem) VALUES (?, ?, ?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.setString(2, device);
      stmt.setString(3, problem);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Error: addITReq", e);
    }
  }

  // Matt
  /**
   * Adds a request for security
   *
   * @param reqNotes Description of the incident being reported
   * @param nodeID The ID of the node in which these services are requested
   * @return the id of the created request
   * @throws DBException If an error occurs
   */
  public static int addSecurityReq(String reqNotes, String nodeID, String isEmergency)
      throws DBException {
    try {
      String query =
          "INSERT INTO request (timeRequested, reqNotes, serviceType, nodeID, status) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
      stmt.setString(2, reqNotes);
      stmt.setString(3, "Security");
      stmt.setString(4, nodeID);
      stmt.setString(5, "OPEN");
      stmt.execute();
      ResultSet rs = stmt.getGeneratedKeys();
      rs.next();
      query = "INSERT INTO secrequest (requestID, isEmergency) VALUES (?, ?)";
      stmt = con.prepareStatement(query);
      int id = rs.getInt("1");
      stmt.setInt(1, id);
      stmt.setString(2, isEmergency);
      stmt.executeUpdate();
      return id;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Error: addSecurityReq", e);
    }
  }

  // Noah
  /**
   * Assigns an employee to a request; the employee must be able to fulfil that request
   *
   * @param employeeID the ID of the employee to be assigned
   * @param requestID The ID of the request to which they will be assigned
   * @throws DBException If an error occurs or if the employee is unable to fulfill the request
   */
  public static void assignToRequest(int employeeID, int requestID) throws DBException {
    try {
      Employee emp = getEmployee(employeeID);
      Request req = getRequest(requestID);
      if (!emp.getServiceType().equals(req.getServiceType())) {
        throw new DBException(
            "Invalid kind of employee! That employee isn't authorized for that kind of job!");
      }
      if (req instanceof TranslatorRequest) {
        String language = req.getAtr1();
        if (!((Translator) emp).getLanguages().contains(language)) {
          throw new DBException(
              "Invalid selection: That translator can't speak the requested langauge");
        }
      }
      String query = "UPDATE request SET assigned_eID = ? WHERE requestID = ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setInt(1, employeeID);
      stmt.setInt(2, requestID);
      if (stmt.executeUpdate() <= 0) throw new DBException("That requestID is invalid!");
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: assignToRequest", e);
    }
  }

  // Chris
  /**
   * Marks a request as completed and done at the time that this function was called
   *
   * @param requestID the ID of the request to be marked as completed
   * @param compNotes notes regarding the completion of the request
   * @throws DBException If an error occurs, or of the request isn't open
   */
  public static void completeRequest(int requestID, String compNotes) throws DBException {
    Request req = getRequest(requestID);
    try {
      if (req instanceof MedicineRequest) {
        Doctor doc = (Doctor) req.getEmp_assigned();
        try {
          if (!(LoginDB.currentLogin().equals(doc.getUsername()))) {
            throw new DBException(
                "Error: You muse login as the Doctor "
                    + doc.getName()
                    + " with username "
                    + doc.getUsername());
          }
        } catch (DBException e) {
          throw new DBException("Error: No login");
        }
      }
      String query = "SELECT status FROM request WHERE requestID = ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setInt(1, requestID);
      ResultSet rs = stmt.executeQuery();
      rs.next();
      if (!rs.getString("status").equals("OPEN")) {
        throw new DBException("That request isn't open!");
      }
      query =
          "UPDATE request SET status = 'DONE', timeCompleted = ?, compNotes = ? WHERE requestID = ?";
      stmt = con.prepareStatement(query);
      stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
      stmt.setInt(3, requestID);
      stmt.setString(2, compNotes);
      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Error: completeRequest", e);
    }
  }

  // Nick
  /**
   * Removes an employee from the database
   *
   * @param employeeID the id of the employee to be excised
   * @throws DBException If an error occurs, or if the id is invalid
   */
  public static void removeEmployee(int employeeID) throws DBException {
    try {
      String query = "DELETE FROM employees WHERE employeeID = ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setInt(1, employeeID);
      if (stmt.executeUpdate() <= 0) throw new DBException("That employeeID is invalid!");
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: removeEmployee", e);
    }
  }

  // regEx
  /**
   * Adds a language to the translator with the specified employee ID
   *
   * @param employeeID The ID of the employee who will speak this new language
   * @param language The language to be added
   * @throws DBException if the employee isn't a translator, or if an error occurs
   */
  public static void addLanguage(int employeeID, String language) throws DBException {
    try {
      String query = "INSERT INTO language VALUES(?, ?)";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setInt(1, employeeID);
      stmt.setString(2, language);
      stmt.executeUpdate();
    } catch (SQLException e) {
      if (e.getSQLState()
          .equals("23503")) { // foreign key violation (so the employeeID isn't in translator)
        throw new DBException("Error: Translator by ID " + employeeID + " does not exist!");
      }
      e.printStackTrace();
      throw new DBException("Unknown error: addLanguage, eid = " + employeeID, e);
    }
  }

  // Chris
  /**
   * Removes a language to the translator with the specified employee ID
   *
   * @param employeeID The ID of the employee who no longer speaks the given language
   * @param language The language to be removed
   * @throws DBException if the employee isn't a translator, or if an error occurs
   */
  public static void removeLanguage(int employeeID, String language) throws DBException {
    try {
      String query = "DELETE FROM language WHERE t_employeeID = ? AND language = ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setInt(1, employeeID);
      stmt.setString(2, language);
      if (stmt.executeUpdate() <= 0) throw new DBException("That translator doesn't exist");
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException(
          "Unknown Error: removeLanguaguage, ID: " + employeeID + " Lang: " + language, e);
    }
  }

  // Nick
  /**
   * Denies a given request
   *
   * @param requestID The request id of an open request to deny
   * @param compNotes Notes on the denial of this request
   * @throws DBException if the request isn't open, or if there was an error
   */
  public static void denyRequest(int requestID, String compNotes) throws DBException {
    try {
      String query = "SELECT status FROM request WHERE requestID = ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setInt(1, requestID);
      ResultSet rs = stmt.executeQuery();
      rs.next();
      if (!rs.getString("status").equals("OPEN")) {
        throw new DBException("That request isn't open!");
      }

      query =
          "UPDATE request SET status = 'DENY', timeCompleted = ?, compNotes = ? WHERE requestID = ?";
      stmt = con.prepareStatement(query);
      stmt.setTimestamp(1, new Timestamp(new Date().getTime()));
      stmt.setInt(3, requestID);
      stmt.setString(2, compNotes);
      stmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Error: completeRequest", e);
    }
  }

  /**
   * Returns all the languages that are currently available
   *
   * @return a linked list of strings representing all the languages available
   * @throws DBException If an error occurs
   */
  public static LinkedList<String> getLanguages() throws DBException {
    try {
      String query = "SELECT DISTINCT language FROM language";
      PreparedStatement stmt = con.prepareStatement(query);
      ResultSet rs = stmt.executeQuery();
      LinkedList<String> langs = new LinkedList<>();
      while (rs.next()) {
        langs.add(rs.getString("language"));
      }
      return langs;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: getlanguages", e);
    }
  }

  /**
   * Changes the time that a service is available. Important: Times must be in a five-character time
   * format in 24-hour time Examples of valid times: 08:45, 14:20, 00:15 (12:15 AM), invalid times:
   * 8:45, 2:20PM, 12:15 would be 15 minutes past noon
   *
   * @param serviceType The service type which you want to change
   * @param startTime The new start time for the service
   * @param endTime The new end time for the service
   * @throws DBException On error or when input is invalid
   */
  public static void setServiceTime(String serviceType, String startTime, String endTime)
      throws DBException {
    String p = "([01]\\d:[0-6]\\d)|(2[0-4]:[0-6]\\d)";
    Pattern pattern = Pattern.compile(p);
    if (startTime.length() == 5
        && endTime.length() == 5
        && pattern.matcher(startTime).matches()
        && pattern.matcher(startTime).matches()) {
      String query = "UPDATE service SET timeStart = ?, timeEnd = ? WHERE serviceType = ?";
      try {
        PreparedStatement stmt = con.prepareStatement(query);
        stmt.setString(1, startTime);
        stmt.setString(2, endTime);
        stmt.setString(3, serviceType);
        if (stmt.executeUpdate() <= 0) throw new DBException("That service type is invalid!");
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DBException("Unknown error: setServiceTime ", e);
      }
    } else
      throw new DBException(
          "The times you entered, " + startTime + ", " + endTime + ", are invalid!");
  }

  /**
   * Converts a Timestamp object to a GregorianCalendar object representing the same time
   *
   * @param time The Timestamp object to be converted
   * @return a GregorianCalendar representing the same time as the Timestamp passed in
   */
  public static GregorianCalendar getJavatime(Timestamp time) {
    if (time == null) {
      return null;
    }
    Calendar cal = GregorianCalendar.getInstance();
    cal.setTime(time);
    return (GregorianCalendar) cal;
  }

  /**
   * Converts a GregorianCalendar object to a Timestamp object representing the same time
   *
   * @param cal The GregorianCalendar object to be converted
   * @return A Timestamp representing the same time as the GregorianCalendar passed in
   */
  public static Timestamp getSqltime(GregorianCalendar cal) {
    return new Timestamp(cal.getTime().getTime());
  }

  // Chris

  /**
   * Gets a list of patients taking the specified medicine
   *
   * @param type The type of medicine
   * @return a LinkedList of patients
   * @throws DBException If an error occurs
   */
  public static LinkedList<String> getPatientByMedType(String type) throws DBException {
    try {
      String query = "SELECT patient FROM medicineRequests WHERE UPPER(medicineName) = ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setString(1, type.toUpperCase());
      ResultSet rs = stmt.executeQuery();
      LinkedList<String> plist = new LinkedList<>();
      while (rs.next()) {
        plist.add(rs.getString("patient"));
      }
      return plist;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Error: getpatientbyMedType");
    }
  }

  // Nick

  /**
   * Gets a list of requests associated with the specified patient
   *
   * @param patient The specified patient
   * @return a LinkedList of MedicineRequest
   * @throws DBException If an error occurs
   */
  public static LinkedList<MedicineRequest> getMedRequestByPatient(String patient)
      throws DBException {
    try {
      LinkedList<MedicineRequest> res = new LinkedList<>();

      String query =
          "SELECT * FROM medicineRequests "
              + "JOIN request ON medicineRequests.requestID = request.requestID "
              + "WHERE patient = ?";
      PreparedStatement st = con.prepareStatement(query);
      st.setString(1, patient);
      ResultSet rs = st.executeQuery();

      while (rs.next()) {
        res.add(
            new MedicineRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("medicineName"),
                rs.getDouble("dosage"),
                rs.getString("units"),
                rs.getString("patient")));
      }

      return res;
    } catch (SQLException e) {
      throw new DBException("Unknown error: getMedRequest", e);
    }
  }

  // Chris
  /**
   * gets a list of sanitationRequest where the size matches the given val (case-insensitive)
   *
   * @param size The size of the spill
   * @return a list of sanitationRequest where size matches the given size
   * @throws DBException If an error occurs
   */
  public static LinkedList<SanitationRequest> getsanitationbyAmount(String size)
      throws DBException {
    LinkedList<SanitationRequest> list = new LinkedList<>();
    try {
      String query =
          "SELECT * FROM sanitationRequests, request WHERE sanitationRequests.requestID = request.requestID AND Upper(size) = ?";
      PreparedStatement st = con.prepareStatement(query);
      st.setString(1, size.toUpperCase());
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        list.add(
            new SanitationRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("sanitationType"),
                rs.getString("size"),
                rs.getString("danger")));
      }
      return list;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown Error: getSanitationAmount");
    }
  }

  //  // Nick
  //  /**
  //   * Adds a patient to the database
  //   *
  //   * @param name The name of the patient
  //   * @param location The nodeID of the location of the patient
  //   * @return id of created patient
  //   */
  //  public static int addPatient(String name, String location) throws DBException {
  //    try {
  //      String query = "INSERT INTO patients (patientName, location) VALUES (?, ?)";
  //      PreparedStatement st = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
  //      st.setString(1, name);
  //      st.setString(2, location);
  //      st.executeUpdate();
  //      ResultSet rs = st.getGeneratedKeys();
  //      rs.next();
  //      int id = rs.getInt("1");
  //      return id;
  //    } catch (SQLException e) {
  //      throw new DBException("Unknown error: addPatient", e);
  //    }
  //  }
  //
  //  // Chris
  //  /**
  //   * gets list of all patients
  //   * @return lst of all patients
  //   */
  //  public static LinkedList<Patient> getlistPatient() throws DBException {
  //    LinkedList<Patient> allPatients = new LinkedList<Patient>();
  //
  //    try{
  //      String query = "SELECT * FROM patients";
  //      PreparedStatement stmt = con.prepareStatement(query);
  //      ResultSet rs = stmt.executeQuery();
  //      while(rs.next()){
  //        allPatients.add(
  //                new Patient(
  //                        rs.getInt("id"),
  //                        rs.getString("name"),
  //                        rs.getString("location")
  //                )
  //        );
  //      }
  //      return allPatients;
  //    } catch (SQLException e) {
  //      e.printStackTrace();
  //      throw new DBException("Error: getlistPatients");
  //    }
  //  }

  // Nick
  //  public static Patient getPatient(int patientID) throws DBException {
  //    try {
  //      String query = "SELECT * FROM patients WHERE patientID = ?";
  //      PreparedStatement st = con.prepareStatement(query);
  //      st.setInt(1, patientID);
  //      ResultSet rs = st.executeQuery();
  //      if (rs.next()) {
  //        return new Patient(
  //            rs.getInt("patientID"), rs.getString("patientName"), rs.getString("location"));
  //      } else {
  //        throw new DBException("getPatient: Could not find patient with id " + patientID);
  //      }
  //    } catch (SQLException e) {
  //      throw new DBException("Unknown error: getPatient", e);
  //    }
  //  }

  //// Chris
  //  /**
  //   * gets a list of patient with the specified name
  //   * @param id
  //   * @return list of patients
  //   */
  //  public static LinkedList<String> searchbyPatient(int id) throws DBException {
  //    try{
  //      LinkedList<String> patients = new LinkedList<>();
  //      String query = "SELECT * FROM patients WHERE id = ?";
  //      PreparedStatement stmt = con.prepareStatement(query);
  //      stmt.setInt(1, id);
  //      ResultSet rs = stmt.executeQuery();
  //      while(rs.next()){
  //        patients.add(
  //                new Patient(
  //                        rs.getInt("id"),
  //                        rs.getString("name"),
  //                        rs.getString("location")
  //                )
  //        );
  //      }
  //      return patients;
  //    } catch (SQLException e) {
  //      e.printStackTrace();
  //      throw new DBException("Error: searchbyPatient causing error");
  //    }
  //  }

  // Nick

  /**
   * Searches for a medicine name (case-insensitive)
   *
   * @param searchQuery The search query
   * @return LinkedList of medicine name (String) that matches query
   * @throws DBException If an error occurs
   */
  public static LinkedList<String> searchByMedType(String searchQuery) throws DBException {
    try {
      LinkedList<String> res = new LinkedList<>();
      String query = "SELECT medicineName FROM medicineRequests WHERE UPPER(medicineName) LIKE ?";
      PreparedStatement st = con.prepareStatement(query);
      st.setString(1, "%" + searchQuery.toUpperCase() + "%");
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        res.add(rs.getString("medicineName"));
      }
      return res;
    } catch (SQLException e) {
      throw new DBException("Unknown error : searchByMedType", e);
    }
  }

  // Chris
  /**
   * Searches through the database where the spillType contains the given type (case-insensitive)
   *
   * @param type The type of spill
   * @return a list of sanitationRequest where spillType contains the given type
   * @throws DBException If an error occurs
   */
  public static LinkedList<SanitationRequest> searchbyspillType(String type) throws DBException {
    try {
      LinkedList<SanitationRequest> list = new LinkedList<>();
      String query = "SELECT * FROM sanitationRequests WHERE UPPER(sanitationType) LIKE ?";
      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setString(1, "%" + type.toUpperCase() + "%");
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        list.add((SanitationRequest) getRequest(rs.getInt("requestID")));
      }
      return list;
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DBException("Unknown error: searchbyspillType");
    }
  }

  // Nick
  /**
   * Adds a sanitation request with the specified fields
   *
   * @param reqNotes The notes of the request
   * @param nodeID The node id of the location the request needs to be sent to
   * @param spillType The type of spill
   * @param size The size of the spill (one of "small", "medium", "large", "unknown";
   *     case-insensitive)
   * @param danger The danger level of the spill (one of "low", "medium", "high", "unknown";
   *     case-insensitive)
   * @return The id of the newly created request
   * @throws DBException If the size or danger level is invalid, or if an error occurs
   */
  public static int addSanitationReq(
      String reqNotes, String nodeID, String spillType, String size, String danger)
      throws DBException {
    size = size.toLowerCase();
    danger = danger.toLowerCase();

    String[] sizeArray = new String[] {"small", "medium", "large", "unknown"};
    String[] dangerArray = new String[] {"low", "medium", "high", "unknown"};

    if (!Arrays.asList(sizeArray).contains(size)) {
      throw new DBException("addSanitationReq: \"" + size + "\" is not a valid size");
    }

    if (!Arrays.asList(dangerArray).contains(danger)) {
      throw new DBException("addSanitationReq: \"" + danger + "\" is not a valid danger level");
    }

    try {
      con.setAutoCommit(false);

      String query =
          "INSERT INTO request (timeRequested, reqNotes, serviceType, nodeID, status) VALUES (?, ?, ?, ?, ?)";
      PreparedStatement st = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      st.setTimestamp(1, new Timestamp(new Date().getTime()));
      st.setString(2, reqNotes);
      st.setString(3, "Sanitation");
      st.setString(4, nodeID);
      st.setString(5, "OPEN");
      st.executeUpdate();

      ResultSet rs = st.getGeneratedKeys();
      rs.next();
      int id = rs.getInt("1");

      query =
          "INSERT INTO sanitationRequests (requestid, size, sanitationtype, danger) VALUES (?, ?, ?, ?)";
      st = con.prepareStatement(query);
      st.setInt(1, id);
      st.setString(2, size);
      st.setString(3, spillType);
      st.setString(4, danger);
      st.executeUpdate();

      con.commit();
      con.setAutoCommit(true);
      return id;
    } catch (SQLException e) {
      try {
        con.rollback();
        con.setAutoCommit(true);
      } catch (SQLException ex) {
        throw new DBException("Unknown Error: addSanitationReq", ex);
      }
      throw new DBException("Unknown Error: addSanitationReq", e);
    }
  }
  // Nick
  /**
   * Gets a list of SanitationRequest where the danger matches the given value (case-insensitive)
   *
   * @param danger The danger level of the sanitation request
   * @return A list of SanitationRequest where danger matches the given danger level
   * @throws DBException If an error occurs
   */
  public static LinkedList<SanitationRequest> getSanitationByDanger(String danger)
      throws DBException {
    try {
      LinkedList<SanitationRequest> result = new LinkedList<>();

      danger = danger.toLowerCase();

      String query =
          "SELECT * FROM sanitationRequests "
              + "JOIN request ON sanitationRequests.requestID = request.requestID "
              + "WHERE LOWER(danger) = ?";

      PreparedStatement st = con.prepareStatement(query);
      st.setString(1, danger);
      ResultSet rs = st.executeQuery();

      while (rs.next()) {
        result.add(
            new SanitationRequest(
                rs.getInt("requestID"),
                rs.getInt("assigned_eID"),
                rs.getString("reqNotes"),
                rs.getString("compNotes"),
                rs.getString("nodeID"),
                getJavatime(rs.getTimestamp("timeRequested")),
                getJavatime(rs.getTimestamp("timeCompleted")),
                rs.getString("status"),
                rs.getString("sanitationType"),
                rs.getString("size"),
                rs.getString("danger")));
      }
      return result;
    } catch (SQLException e) {
      throw new DBException("Unknown error: getSanitationByDanger", e);
    }
  }

  // Nick
  /**
   * Gets a list of all sanitation Employees in the database
   *
   * @return a LinkedList of Sanitation
   * @throws DBException If an error occurs
   */
  public static LinkedList<Sanitation> getSanitationEmp() throws DBException {
    try {
      LinkedList<Sanitation> result = new LinkedList<>();
      String query = "SELECT * FROM employees WHERE SERVICETYPE = 'Sanitation'";

      PreparedStatement st = con.prepareStatement(query);
      ResultSet rs = st.executeQuery();

      while (rs.next()) {
        result.add(new Sanitation(rs.getInt("employeeID"), rs.getString("name")));
      }
      return result;
    } catch (SQLException e) {
      throw new DBException("Unknown error: getSanitationEmp", e);
    }
  }
}
