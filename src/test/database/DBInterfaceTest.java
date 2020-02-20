package database;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DBInterfaceTest {

  private static DBInterface db;

  @BeforeClass
  public static void connectToDB() {
    System.out.println("here");
    try {
      db = new DBInterface();
    } catch (DBInitializationException e) {
      System.out.println("exception");
    }
    System.out.println("now here");
  }

  @Test
  public void test1() {
    assertEquals(1, 1);
  }
}
