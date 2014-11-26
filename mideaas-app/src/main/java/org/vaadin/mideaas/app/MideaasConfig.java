package org.vaadin.mideaas.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.vaadin.mideaas.ide.model.UserSettings;

public class MideaasConfig {
	
	@SuppressWarnings("serial")
	public static class ConfigError extends RuntimeException {
		public ConfigError(File config, String message) {
			super(msg(message,config));
		}
		public ConfigError(File config, String message, Throwable cause) {
			super(msg(message,config), cause);
		}
		private static String msg(String message, File config) {
			if (config==null) {
				return message + "\n... when reading default config file at mideaas-app/src/main/resources/"+MIDEAAS_CONFIG_FILE_IN_CLASSPATH;
			}
			else {
				return message + "\n... when reading config file "+config;
			}
		}
	}
    
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
        PAAS_DEPLOY, 
        GAE_COMPILE,
        COAPS_API_URI, 
        USE_SLA_SELECTION_MAP, 
        SLA_SELECTION_MAP_URI,
        
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
                    readDefaultConfig();
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
        readConfigFrom(new FileInputStream(f));
        checkProperties(f);
    }
    
    private static void readConfigFrom(InputStream inputStream) throws IOException {
    	try {
            properties.load(inputStream);
        }
        finally {
            if (inputStream!=null) {
            	inputStream.close();
            }
        }
    }

    private static void readDefaultConfig() {
        InputStream inputStream = MideaasConfig.class.getClassLoader()
                .getResourceAsStream(MIDEAAS_CONFIG_FILE_IN_CLASSPATH);
    
        if (inputStream == null) {
        	throw new ConfigError(new File(MIDEAAS_CONFIG_FILE_IN_CLASSPATH), "Could not load properties file.");
        }
        else {
            try {
                readConfigFrom(inputStream);
                checkProperties(null);
            } catch (IOException e) {
            	throw new ConfigError(new File(MIDEAAS_CONFIG_FILE_IN_CLASSPATH), "Could not read", e);
            }
        }
    }

    public static boolean easiCloudsFeaturesTurnedOn(){
    	return "on".equals(MideaasConfig.getProperty(Prop.EASICLOUDS_FEATURES));
    }

    public static boolean compileGaeTurnedOn(){
    	return "on".equals(MideaasConfig.getProperty(Prop.GAE_COMPILE));
    }

    public static boolean paasDeployTurnedOn(){
    	String value = MideaasConfig.getProperty(Prop.PAAS_DEPLOY);
    	boolean boolValue = "on".equals(value);
    	return boolValue;
    }
    
    public static boolean isExperiment() {
    	return "on".equals(MideaasConfig.getProperty(Prop.EXPERIMENT));
    }

	public static File getLogDir() {
		String d = MideaasConfig.getProperty(Prop.LOG_DIR);
		return d==null ? null : new File(d);
	}

	public static UserSettings getDefaultUserSettings() {
		boolean ecFeaturesOn=MideaasConfig.easiCloudsFeaturesTurnedOn();
		boolean pdOn=MideaasConfig.paasDeployTurnedOn();
		boolean cgon=MideaasConfig.compileGaeTurnedOn();
		String coapsApiUri = MideaasConfig.coapsApiUri();
		boolean useSLASelectionMap = MideaasConfig.useSLASelectionMap();
		String slaSelectionMapUri = MideaasConfig.slaSelectionMapUri();
		
		UserSettings settings = new UserSettings(coapsApiUri, ecFeaturesOn, pdOn, cgon, useSLASelectionMap, slaSelectionMapUri);
		settings.userAgent = getProperty(Prop.DEFAULT_WIDGETSET_USER_AGENT);
		return settings;
	}

	private static String slaSelectionMapUri() {
    	return MideaasConfig.getProperty(Prop.SLA_SELECTION_MAP_URI);
	}

	private static boolean useSLASelectionMap() {
    	return "on".equals(MideaasConfig.getProperty(Prop.USE_SLA_SELECTION_MAP));
	}

	private static String coapsApiUri() {
    	return MideaasConfig.getProperty(Prop.COAPS_API_URI);
	}

	public static File getFeedbackFile() {
		String d = MideaasConfig.getProperty(Prop.FEEDBACK_FILE);
		return d==null ? null : new File(d);
	}
	
	public static File getProjectsDir() {
		return new File(MideaasConfig.getProperty(Prop.PROJECTS_DIR));
	}
	
	public static String getFNTSServers() {
		String servers = MideaasConfig.getProperty(Prop.FNTS_SERVERS);
		return servers;
	}
	
	public static int getExecutorNumber() {
		int executors = Integer.valueOf(MideaasConfig.getProperty(Prop.EXECUTORS));
		return executors;
	}
	
	public static File getMavenHome() {
		return new File(getProperty(Prop.MAVEN_HOME));
	}
	
	public static void checkProperties(File f) {
		
		if (!getProjectsDir().isDirectory()) {
			throw new ConfigError(f, Prop.PROJECTS_DIR + " does not exist: " + getProjectsDir());
		}
		
		File logDir = getLogDir();
		if (logDir!=null && !logDir.isDirectory()) {
			throw new ConfigError(f, Prop.LOG_DIR + " does not exist: " + logDir);
		}
		
		File mvn = FileUtils.getFile(getMavenHome(), "bin", "mvn");
		if (!mvn.isFile()) {
			throw new ConfigError(f, "Not a proper "+Prop.MAVEN_HOME+": "+getMavenHome()+" - "+mvn+" does not exist.");
		}
		
		// TODO: more checks
	}

	

}
