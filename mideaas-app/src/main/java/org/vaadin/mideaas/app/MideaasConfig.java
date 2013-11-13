package org.vaadin.mideaas.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.vaadin.mideaas.model.UserSettings;

public class MideaasConfig {
    
	//get using: MideaasConfig.getProperty(Prop.JETTY_STOP_PORT_MAX)
    public enum Prop {
        DEPLOY_DIR,
        DEPLOY_SERVER,
        MAVEN_HOME,
        FNTS_SERVERS,
        EXECUTORS,
        PROJECTS_DIR,
        APP_PACKAGE_BASE,
        VISUAL_DESIGNER_URL,
        JETTY_PORT_MIN,
        JETTY_PORT_MAX,
        JETTY_STOP_PORT_MIN,
        JETTY_STOP_PORT_MAX,
        EASICLOUDS_FEATURES,
        EXPERIMENT,
        LOG_DIR,
        DEFAULT_WIDGETSET_USER_AGENT,
        FEEDBACK_FILE,

        GITHUB_KEY,
        GITHUB_SECRET,
        
        FACEBOOK_KEY,
        FACEBOOK_SECRET,
        
        TWITTER_KEY,
        TWITTER_SECRET
    }

    public static final String MIDEAAS_CONFIG_FILE_IN_CLASSPATH = "mideaas.properties";
    
    public static final String ENV_FILE = "MIDEAAS_CONFIG_FILE";
    
    private static final Properties properties = new Properties();
    
    // Default properties:
    static {
        properties.put(Prop.APP_PACKAGE_BASE.toString(), "com.arvue.apps");
    }
    
    static {
        String configFile = System.getenv().get(ENV_FILE);
        if (configFile==null) {
            readDefaultConfig();
        }
        else {
            File f = new File(configFile);
            if (f.exists()) {
                try {
                    readConfigFrom(f);
                } catch (IOException e) {
                    System.err.println("WARNING: error reading config from '" + f +"'. Reading default config.");
                }
            }
            else {
                System.err.println("WARNING: file " + f +" does not exist. Reading default config.");
                readDefaultConfig();
            }
        }
       
    }
    
    public static final String getProperty(Prop p) {
        return properties.getProperty(p.toString());
    }
    
    public static int getPropertyInt(Prop p) {
		return Integer.valueOf(getProperty(p));
	}

    private static void readConfigFrom(File f) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            properties.load(fis);
        }
        finally {
            if (fis!=null) {
                fis.close();
            }
        }
    }

    private static void readDefaultConfig() {
        InputStream inputStream = MideaasConfig.class.getClassLoader()
                .getResourceAsStream(MIDEAAS_CONFIG_FILE_IN_CLASSPATH);
    
        if (inputStream == null) {
            System.err.println("WARNING: could not load properties file: " + MIDEAAS_CONFIG_FILE_IN_CLASSPATH);
        }
        else {
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                System.err.println("WARNING: error loading properties file: " + e.getMessage());
            }
        }
    }

    public static boolean easiCloudsFeaturesTurnedOn(){
    	return "on".equals(MideaasConfig.getProperty(Prop.EASICLOUDS_FEATURES));
    }
    
    public static boolean isExperiment() {
    	return "on".equals(MideaasConfig.getProperty(Prop.EXPERIMENT));
    }

	public static File getLogDir() {
		String d = MideaasConfig.getProperty(Prop.LOG_DIR);
		return d==null ? null : new File(d);
	}

	public static UserSettings getDefaultUserSettings() {
		UserSettings settings = new UserSettings();
		settings.userAgent = getProperty(Prop.DEFAULT_WIDGETSET_USER_AGENT);
		return settings;
	}

	public static File getFeedbackFile() {
		String d = MideaasConfig.getProperty(Prop.FEEDBACK_FILE);
		return d==null ? null : new File(d);
	}
	
	public static String getProjectsDir() {
		String dir = MideaasConfig.getProperty(Prop.PROJECTS_DIR);
		return dir;
	}
	
	public static String getFNTSServers() {
		String servers = MideaasConfig.getProperty(Prop.FNTS_SERVERS);
		return servers;
	}
	
	public static int getExecutorNumber() {
		int executors = Integer.valueOf(MideaasConfig.getProperty(Prop.EXECUTORS));
		return executors;
	}

}
