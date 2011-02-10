package wyjs.testing;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import wyjs.Main;

public class TestHarness {

  private String srcPath; // path to source files
  private String outputPath; // path to output files
  private String outputExtension; // the extension of output files

  /**
   * Construct a test harness object.
   * 
   * @param srcPath The path to the source files to be tested
   * @param outputPath The path to the sample output files to compare against.
   * @param outputExtension The extension of output files
   * @param verification if true, the verifier is used.
   */
  public TestHarness(String srcPath, String outputPath, String outputExtension) {
    this.srcPath = srcPath.replace('/', File.separatorChar);
    this.outputPath = outputPath.replace('/', File.separatorChar);
    this.outputExtension = outputExtension;
  }

  /**
   * Compile and execute a test case, whilst comparing its output against the
   * sample output.
   * 
   * @param name Name of the test to run. This must correspond to an
   *          executable Java file in the srcPath of the same name.
   */
  protected void runTest(String name, String... params) {
    final String[] args = new String[1 + params.length];
    for (int i = 0; i != params.length; ++i) {
      args[i] = params[i];
    }
    args[args.length - 1] = srcPath + File.separatorChar + name + ".wyjs";

    if (Main.run(args) != 0) {
      fail("couldn't compile test!");
    } else {
      String output = run(srcPath, name, args);
      compare(output, outputPath + File.separatorChar + name + "."
          + outputExtension);
    }
  }

  protected void parserFailTest(String name) {
    name = srcPath + File.separatorChar + name + ".wyjs";

    if (compile("-wp", "lib/wyrt.jar", name) != Main.PARSE_ERROR) {
      fail("Test parsed when it shouldn't have!");
    }
  }

  protected void contextFailTest(String name) {
    name = srcPath + File.separatorChar + name + ".wyjs";

    if (compile("-wp", "lib/wyrt.jar", name) != Main.CONTEXT_ERROR) {
      fail("Test compiled when it shouldn't have!");
    }
  }

  protected void verificationFailTest(String name) {
    name = srcPath + File.separatorChar + name + ".wyjs";

    if (compile("-wp", "lib/wyrt.jar", "-V", name) != Main.CONTEXT_ERROR) {
      fail("Test compiled when it shouldn't have!");
    }
  }

  protected void verificationRunTest(String name) {
    String fullName = srcPath + File.separatorChar + name + ".wyjs";

    if (compile("-wp", "lib/wyrt.jar", "-V", fullName) != 0) {
      fail("couldn't compile test!");
    } else {
      String output = run(srcPath, name, "-wp", "lib/wyrt.jar");
      compare(output, outputPath + File.separatorChar + name + "."
          + outputExtension);
    }
  }

  protected void runtimeFailTest(String name) {
    String fullName = srcPath + File.separatorChar + name + ".wyjs";
    if (compile("-wp", "lib/wyrt.jar", fullName) != 0) {
      fail("couldn't compile test!");
    } else {
      String output = run(srcPath, name, "-wp", "lib/wyrt.jar");
      if (output != null) {
        fail("test should have failed at runtime!");
      }
    }
  }

  private static int compile(String... args) {
    return Main.run(args);
  }

  private static String run(String path, String name, String... args) {
    try {
      Reader stdlib = new FileReader(new File("lib/stdlib.min.js"));
      Reader file = new FileReader(new File(path + "/" + name + ".js"));
      Context cxt = Context.enter();
      Scriptable scope = cxt.initStandardObjects();

      OutputStream out = new ByteArrayOutputStream();
      Object sysout = Context.javaToJS(new PrintStream(out), scope);
      OutputStream err = new ByteArrayOutputStream();
      Object syserr = Context.javaToJS(new PrintStream(err), scope);

      ScriptableObject.putConstProperty(scope, "sysout", sysout);
      ScriptableObject.putConstProperty(scope, "syserr", syserr);
      cxt.evaluateReader(scope, stdlib, "stdlib", 1, null);
      cxt.evaluateReader(scope, file, name, 1, null);

      System.err.println(err);
      return out.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
      fail("Problem running compiled test");
    } finally {
      Context.exit();
    }

    return null;
  }

  /**
   * Compare the output of executing java on the test case with a reference
   * file.
   * 
   * @param output This provides the output from executing java on the test
   *          case.
   * @param referenceFile The full path to the reference file. This should use
   *          the appropriate separator char for the host operating system.
   */
  private static void compare(String output, String referenceFile) {
    try {
      BufferedReader outReader = new BufferedReader(new StringReader(output));
      BufferedReader refReader =
          new BufferedReader(new FileReader(new File(referenceFile)));

      while (refReader.ready() && outReader.ready()) {
        String a = refReader.readLine();
        String b = outReader.readLine();

        if (a.equals(b)) {
          continue;
        } else {
          System.err.println(" > " + a);
          System.err.println(" < " + b);
          throw new Error("Output doesn't match reference");
        }
      }

      String l1 = outReader.readLine();
      String l2 = refReader.readLine();
      if (l1 == null && l2 == null) {
        return;
      }

      do {
        l1 = outReader.readLine();
        l2 = refReader.readLine();
        if (l1 != null) {
          System.err.println(" < " + l1);
        } else if (l2 != null) {
          System.err.println(" > " + l2);
        }
      } while (l1 != null && l2 != null);

      fail("Files do not match");
    } catch (Exception ex) {
      ex.printStackTrace();
      fail();
    }
  }

  public static class StreamGrabber extends Thread {

    private InputStream input;
    private StringBuffer buffer;

    StreamGrabber(InputStream input, StringBuffer buffer) {
      this.input = input;
      this.buffer = buffer;
      start();
    }

    public void run() {
      try {
        int nextChar;
        // keep reading!!
        while ((nextChar = input.read()) != -1) {
          buffer.append((char) nextChar);
        }
      } catch (IOException ioe) {}
    }
  }
}
