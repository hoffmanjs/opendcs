package org.opendcs.fixtures.helpers;

import java.io.File;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import decodes.tsdb.CpCompDependsUpdater;
import decodes.tsdb.TsdbAppTemplate;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;



/**
 * Make it easier for a test to start required background applications.
 * TODO: it would be nice for those apps to run in the same JVM; however, at
 * the moment each assumes it's the only "OpenDCS" process running so that
 * will have to wait until we have more of this testing in place.
 */
public class BackgroundTsDbApp<App extends TsdbAppTemplate> {
    private static final Logger log = Logger.getLogger(BackgroundTsDbApp.class.getName());

    private String name;
    private Process app;

    public static <App extends TsdbAppTemplate> BackgroundTsDbApp<App> forApp(
            Class<App> clazz, String name, File propertiesFile, File logFile, EnvironmentVariables env,
            String... args)
        throws Exception
    {
        BackgroundTsDbApp<App> bga = new BackgroundTsDbApp<App>(clazz, name, propertiesFile, logFile,
                                                                env, args);
        return bga;
    }

    public static <App extends TsdbAppTemplate> void waitForApp(Class<App> clazz, String name, File propertiesFile, File logFile,
                                  EnvironmentVariables env, long waitTime, TimeUnit waitTimeUnit, String... args) throws Exception, TimeoutException
    {
        BackgroundTsDbApp<App> app = BackgroundTsDbApp.forApp(clazz, name, propertiesFile,logFile,env,args);
        long start = System.currentTimeMillis();
        long now = System.currentTimeMillis();
        do
        {
            try
            {
                Thread.sleep(2000); // wait 2 seconds before checking again
                now = System.currentTimeMillis();
            }
            catch (InterruptedException ex)
            {
                /* do nothing, loop again */
            }
        }
        while(app.isRunning() && (now - start) < waitTimeUnit.toMillis(waitTime));

        if (app.isRunning() && (now - start) > waitTimeUnit.toMillis(waitTime))
        {
            throw new TimeoutException("Application, " + clazz.getName() + "(" + name +") did not finish in the prescribed time.");
        }
    }

    private BackgroundTsDbApp(Class<?> clazz, String name, File propertiesFile, File logFile, EnvironmentVariables env,
                           String ...args) throws Exception
    {
        ArrayList<String> theArgs = new ArrayList<>();
        theArgs.add("java");
        theArgs.add("-cp");
        theArgs.add(System.getProperty("opendcs.test.classpath")); // setup classpath
        theArgs.add("-DDCSTOOL_USERDIR="+propertiesFile.getParent());
        theArgs.add("-DDCSTOOL_HOME="+System.getProperty("DCSTOOL_HOME"));
        theArgs.add(clazz.getName());
        theArgs.add("-a"); theArgs.add(name);
        theArgs.add("-l"); theArgs.add(logFile.getAbsolutePath());
        theArgs.add("-P"); theArgs.add(propertiesFile.getAbsolutePath());
        theArgs.add("-d3");
        for (String arg: args)
        {
            theArgs.add(arg);
        }
        ProcessBuilder pb = new ProcessBuilder(theArgs.toArray(new String[0]));
        this.name = name;
        Map<String,String> processEnv = pb.environment();
        processEnv.putAll(env.getVariables());
        app = pb.start();
    }

    public void stop() throws Exception
    {
        synchronized(app)
        {
            app.destroyForcibly();
        }
    }

    public boolean isRunning()
    {
        synchronized(app)
        {
            return app.isAlive();
        }
    }
}
