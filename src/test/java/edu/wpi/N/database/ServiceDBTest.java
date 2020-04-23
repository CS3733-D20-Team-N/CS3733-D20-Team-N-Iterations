package edu.wpi.N.database;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.N.entities.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ServiceDBTest {
  static int laundReqID1, transReqID1;
  static Translator felix;
  static Translator fats;
  static Laundry snaps;

  @BeforeAll
  public static void setup() throws DBException, SQLException, ClassNotFoundException {
    MapDB.initTestDB();

    LinkedList<String> langs = new LinkedList<>();
    langs.add("Gnomish");
    langs.add("Lojban");
    ServiceDB.addTranslator("Felix Bignoodle", langs);
    felix = new Translator(1, "Felix Bignoodle", langs);

    LinkedList<String> langs2 = new LinkedList<>();
    langs2.add("Gnomish");
    ServiceDB.addTranslator("Fats Rumbuckle", langs2);
    fats = new Translator(2, "Fats Rumbuckle", langs2);

    ServiceDB.addLaundry("Snaps McKraken");
    snaps = new Laundry(3, "Snaps McKraken");
    MapDB.addNode("ZHALL00101", 10, 10, 1, "Faulkner", "HALL", "HALLZ1", "HALLZ1", 'Z');
    MapDB.addNode("ZHALL00102", 10, 10, 2, "Faulkner", "HALL", "HALLZ2", "HALLZ2", 'Z');
    laundReqID1 = ServiceDB.addLaundReq("wash", "ZHALL00101");
    transReqID1 = ServiceDB.addTransReq("speak", "ZHALL00102", "Gnomish");
  }

  @Test
  public void testgetlistEmployees() throws DBException {
    LinkedList<Employee> list = ServiceDB.getEmployees();
    assertEquals(3, list.size());
    int id = ServiceDB.addLaundry("Joshua Aloeface");
    list = ServiceDB.getEmployees();
    assertEquals(4, list.size());
    assertTrue(list.contains(new Laundry(id, "Joshua Aloeface")));
    ServiceDB.removeEmployee(id);
  }

  @Test
  public void testaddLanguage() throws DBException {
    ServiceDB.addLanguage(fats.getID(), "Chinese");
    // assertEquals("Chinese", fats.getLanguages().get(1));
    fats = (Translator) ServiceDB.getEmployee(fats.getID());
    assertTrue(fats.getLanguages().contains("Chinese"));
    ServiceDB.removeLanguage(fats.getID(), "Chinese");
    fats = (Translator) ServiceDB.getEmployee(fats.getID());
  }

  @Test
  public void testremoveLanguage() throws DBException {
    ServiceDB.removeLanguage(felix.getID(), "Gnomish");
    // assertNull(felix.getLanguages().get(0));
    felix = (Translator) ServiceDB.getEmployee(felix.getID());
    assertFalse(felix.getLanguages().contains("Gnomish"));
    ServiceDB.addLanguage(felix.getID(), "Gnomish");
    felix = (Translator) ServiceDB.getEmployee(felix.getID());
  }

  @Test
  public void testgetOpenRequest() throws DBException {
    // MapDB.addNode("NSERV00104", 11, 11, 4, "Faulkner", "SERV", "Longname", "ShortName",
    // 'N');
    MapDB.addNode("NDEPT00302", 22, 22, 2, "Faulkner", "DEPT", "Longname1", "Shortname1", 'N');
    // ServiceDB.addLaundReq("Make it extra clean", "NSERV00104");
    ServiceDB.addTransReq("Need a translator for medicine description", "NDEPT00302", "Korean");
    LinkedList<Request> list = ServiceDB.getOpenRequests();
    assertEquals(3, list.size());
    assertFalse((list.contains(ServiceDB.getRequest(transReqID1))));
    assertTrue((list.contains(ServiceDB.getRequest(laundReqID1))));
  }

  @Test
  public void testaddTransReq() throws DBException {
    MapDB.addNode("NDEPT00104", 100, 100, 4, "Faulkner", "DEPT", "Longname", "shortname", 'N');
    int id =
        ServiceDB.addTransReq(
            "Need a Korean translator for prescription",
            MapDB.getNode("NDEPT00104").getNodeID(),
            "Korean");
    assertEquals("NDEPT00104", ServiceDB.getRequest(id).getNodeID());
  }

  @Test
  public void testCompleteRequest() throws DBException {
    ServiceDB.completeRequest(transReqID1);
    Request req = ServiceDB.getRequest(transReqID1);
    assertNotNull(req.getTimeCompleted());
  }

  @Test
  public void testDenyRequest() throws DBException {
    assertThrows(
        DBException.class,
        () -> {
          ServiceDB.denyRequest(transReqID1);
        });
    Request req = ServiceDB.getRequest(transReqID1);
    assertNotNull(req.getTimeCompleted());
    // assertEquals("DENY", req.getStatus());
  }

  @Test
  public void testGetEmployee() throws DBException {
    LinkedList<String> langs = new LinkedList<>();
    Translator felix = (Translator) ServiceDB.getEmployee(1);
    Laundry snaps = (Laundry) ServiceDB.getEmployee(3);
    assertEquals(1, felix.getID());
    assertTrue(felix.getName().equals("Felix Bignoodle"));
    assertTrue(felix.getLanguages().contains("Lojban"));
    assertTrue(felix.getLanguages().contains("Gnomish"));
    assertEquals(new Laundry(3, "Snaps McKraken"), snaps);
  }

  @Test
  public void testGetRequest() throws DBException {
    assertEquals("wash", ServiceDB.getRequest(laundReqID1).getNotes());
    assertEquals("ZHALL00101", ServiceDB.getRequest(laundReqID1).getNodeID());
  }

  @Test
  public void testGetTransLang() throws DBException {
    LinkedList<Translator> translators = ServiceDB.getTransLang("Gnomish");
    assertEquals(2, translators.size());
    assertTrue(translators.contains(felix));
    assertTrue(translators.contains(fats));

    translators = ServiceDB.getTransLang("Lojban");
    assertEquals(1, translators.size());
    assertTrue(translators.contains(felix));
    // assertTrue(translators.contains(felix));
  }

  @Test
  public void testGetServices() throws DBException {
    LinkedList<Service> res = ServiceDB.getServices();
    assertEquals(2, res.size());

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    assertEquals(formatter.parse("00:00").toString(), res.get(0).getStartTime().toString());
    assertEquals(formatter.parse("00:00").toString(), res.get(0).getEndTime().toString());
    assertEquals("Translator", res.get(0).getServiceType());
    assertEquals("Make a request for our translation services!", res.get(0).getDescription());

    assertTrue(
        res.contains(
            new Service(
                "00:00", "00:00", "Translator", "Make a request for our translation services!")));
    assertTrue(
        res.contains(
            new Service("00:00", "00:00", "Laundry", "Make a request for laundry services!")));
  }

  @Test
  public void testassigntoRequest() throws DBException {
    ServiceDB.assignToRequest(felix.getID(), transReqID1);
    assertEquals(felix, ServiceDB.getRequest(transReqID1).getEmp_assigned());
    assertThrows(
        DBException.class,
        () -> {
          ServiceDB.assignToRequest(fats.getID(), laundReqID1);
        });
    assertNull(ServiceDB.getRequest(laundReqID1).getEmp_assigned());
  }

  @Test
  public void testAllLangs() throws DBException {
    LinkedList<String> langs = ServiceDB.getLanguages();
    assertTrue(langs.contains("Gnomish"));
    assertTrue(langs.contains("Lojban"));
  }

  @AfterAll
  public static void cleanup() throws DBException {
    MapDB.clearNodes();
    for (int i : getAllEmployeeIds()) {
      ServiceDB.removeEmployee(i);
    }
  }

  private static LinkedList<Integer> getAllEmployeeIds() throws DBException {
    LinkedList<Integer> ids = new LinkedList<Integer>();

    for (Employee employee : ServiceDB.getEmployees()) {
      ids.add(employee.getID());
    }

    return ids;
  }
}
