package org.spideruci.analysis.config.utils;

import org.apache.maven.plugin.logging.SystemStreamLog;

public class Logger extends SystemStreamLog {

    public static final Logger LOG = new Logger();

    public void debug(CharSequence content, Object ... args)
    {
        super.debug(String.format(String.valueOf(content), args));
    }
    
    public void warn(CharSequence content, Object ... args)
    {
        super.warn(String.format(String.valueOf(content), args));
    }
    
    public void error(CharSequence content, Object ... args)
    {
        super.error(String.format(String.valueOf(content), args));
    }
    
    public void throwError(CharSequence content, Object ... args) {
        final String errorMessage = String.format(String.valueOf(content), args);
        super.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }
    
    public void throwErrorIf(boolean condition, CharSequence content, Object ... args) {
        if (condition) {
            final String errorMessage = String.format(String.valueOf(content), args);
            super.error(errorMessage);
            throw new RuntimeException(errorMessage);    
        }
    }
    
    public void info(CharSequence content, Object ... args)
    {
        super.info(String.format(String.valueOf(content), args));
    }

}
