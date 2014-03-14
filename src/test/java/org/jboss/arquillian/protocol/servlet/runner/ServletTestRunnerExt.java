package org.jboss.arquillian.protocol.servlet.runner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.container.test.spi.TestRunnerExt;
import org.jboss.arquillian.container.test.spi.command.Command;
import org.jboss.arquillian.container.test.spi.util.TestRunners;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;

/**
 * ServletTestRunner
 * 
 * The server side executor for the Servlet protocol impl.
 * 
 * Supports multiple output modes ("outputmode"):
 *  - html
 *  - serializedObject 
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServletTestRunnerExt extends HttpServlet
{
   private static final long serialVersionUID = 2L;

   public static final String PARA_TEST_NAME = "testName";
   public static final String PARA_METHOD_NAME = "methodName";
   public static final String PARA_CLASS_NAME = "className";
   public static final String PARA_OUTPUT_MODE = "outputMode";
   public static final String PARA_CMD_NAME = "cmd";
   
   public static final String OUTPUT_MODE_SERIALIZED = "serializedObject";
   public static final String OUTPUT_MODE_HTML = "html";
   
   public static final String CMD_NAME_TEST = "test";
   public static final String CMD_NAME_EVENT = "event";

   static ConcurrentHashMap<String, Command<?>> events;
   static ThreadLocal<String> currentCall;

   @Override
   public void init() throws ServletException
   {
      events = new ConcurrentHashMap<String, Command<?>>();
      currentCall = new ThreadLocal<String>();
   }
   
   @Override
   public void destroy()
   {
      events.clear();
      currentCall.remove();
   }

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      execute(request, response);
   }
   
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      execute(request, response);
   }
   
   protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      String outputMode = OUTPUT_MODE_HTML;
      String cmd = CMD_NAME_TEST;
      try 
      {
         String className = null;
         String methodName = null;
         String testName = null;

         if (request.getParameter(PARA_OUTPUT_MODE) != null)
         {
            outputMode = request.getParameter(PARA_OUTPUT_MODE);
         }
         className = request.getParameter(PARA_CLASS_NAME);
         if (className == null)
         {
            throw new IllegalArgumentException(PARA_CLASS_NAME + " must be specified");
         }
         methodName = request.getParameter(PARA_METHOD_NAME);
         if ( methodName == null)
         {
            throw new IllegalArgumentException(PARA_METHOD_NAME + " must be specified");
         }
         testName = request.getParameter(PARA_TEST_NAME);
         if ( testName == null)
         {
            throw new IllegalArgumentException(PARA_TEST_NAME + " must be specified");
         }
   
         if(request.getParameter(PARA_CMD_NAME) != null)
         {
            cmd = request.getParameter(PARA_CMD_NAME);
         }
   
         currentCall.set(className + methodName + testName);
         
         if(CMD_NAME_TEST.equals(cmd))
         {
            executeTest(response, outputMode, className, methodName, testName);
         }
         else if(CMD_NAME_EVENT.equals(cmd))
         {
            executeEvent(request, response, className, methodName, testName);
         }
         else
         {
            throw new RuntimeException("Unknown value for parameter" + PARA_CMD_NAME + ": " + cmd);
         }

      } 
      catch(Exception e) 
      {
         if(OUTPUT_MODE_SERIALIZED.equalsIgnoreCase(outputMode)) 
         {
            writeObject(createFailedResult(e), response);
         } 
         else 
         {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());  
         }
      }
      finally
      {
         currentCall.remove();
      }
   }

   public void executeTest(HttpServletResponse response, String outputMode, String className, String methodName, String testName)
         throws ClassNotFoundException, IOException
   {
      Class<?> testClass = SecurityActions.getThreadContextClassLoader().loadClass(className);
      TestRunnerExt runner = (TestRunnerExt)TestRunners.getTestRunner();
      TestResult testResult = runner.execute(testClass, methodName, testName);
      if(OUTPUT_MODE_SERIALIZED.equalsIgnoreCase(outputMode)) 
      {
         writeObject(testResult, response);
      } 
      else 
      {
         // TODO: implement a html view of the result
         response.setContentType("text/html");
         response.setStatus(HttpServletResponse.SC_OK);
         PrintWriter writer = response.getWriter();
         writer.write("<html>\n");
         writer.write("<head><title>TCK Report</title></head>\n");
         writer.write("<body>\n");
         writer.write("<h2>Configuration</h2>\n");
         writer.write("<table>\n");
         writer.write("<tr>\n");
         writer.write("<td><b>Method</b></td><td><b>Status</b></td>\n");
         writer.write("</tr>\n");
         
         writer.write("</table>\n");
         writer.write("<h2>Tests</h2>\n");
         writer.write("<table>\n");
         writer.write("<tr>\n");
         writer.write("<td><b>Method</b></td><td><b>Status</b></td>\n");
         writer.write("</tr>\n");

         writer.write("</table>\n");
         writer.write("</body>\n");
      }
   }
   
   public void executeEvent(HttpServletRequest request, HttpServletResponse response, String className, String methodName, String testHint)
      throws ClassNotFoundException, IOException
   {
      String eventKey = className + methodName + testHint;
      
      if(request.getContentLength() > 0)
      {
         response.setStatus(HttpServletResponse.SC_NO_CONTENT);
         ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(request.getInputStream()));
         Command<?> result = (Command<?>)input.readObject();
         
         events.put(eventKey, result);
      }
      else
      {
         if(events.containsKey(eventKey) && events.get(eventKey).getResult() == null)
         {
            response.setStatus(HttpServletResponse.SC_OK);
            ObjectOutputStream output = new ObjectOutputStream(response.getOutputStream());
            output.writeObject(events.remove(eventKey));
            output.flush();
            output.close();
         }
         else
         {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
         }
      }
   }

   private void writeObject(Object object, HttpServletResponse response) 
   {
      try 
      {
         // Set HttpServletResponse status BEFORE getting the output stream
         response.setStatus(HttpServletResponse.SC_OK);
         ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
         oos.writeObject(object);
         oos.flush();
         oos.close();
      } 
      catch (Exception e) 
      {
         try 
         {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
         } 
         catch (Exception e2) 
         {
            throw new RuntimeException("Could not write to output", e2);
         }
      }
   }
   
   private TestResult createFailedResult(Throwable throwable)
   {
      return new TestResult(Status.FAILED, throwable);
   }
}
