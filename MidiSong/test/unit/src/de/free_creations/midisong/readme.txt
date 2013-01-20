The classes contained in this folder perform unit testing.

For more information on unit-testing in NetBeans see:

http://platform.netbeans.org/tutorials/nbm-test.html


Jaroslav Tulach: Test Patterns In Java
http://openide.netbeans.org/tutorial/test-patterns.html

an example:
http://www.java2s.com/Open-Source/Java-Document/IDE-Netbeans/openide/org/openide/text/CloneableEditorUserQuestionTest.java.htm

A mostly empty FAQ
http://wiki.netbeans.org/NetBeansDeveloperTestFAQ

  but some interesting articels:
  http://wiki.netbeans.org/ModuleDependenciesForTests

  -- running tests in NetBeans Runtime Container:
       public class YourTest extends NbTestCase {
          public YourTest(String s) { super(s); }

          public static Test suite() {
             return NbModuleSuite.create(YourTest.class);
          }

          public void testXYZ() { ... }
          public void testABC() { ... }
       }

  use the Filesystems API
  Writing Tests For DataObjects and DataLoaders
  http://wiki.netbeans.org/UsingFileSystemsMasterfs
  http://wiki.netbeans.org/TestingThingsThatUseFileObjectDataObjectDataFolder
  http://wiki.netbeans.org/DevFaqTestDataObject


