package fr.sewatech.example.jta;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import org.jboss.util.naming.NonSerializableFactory;
import org.jnp.interfaces.NamingContext;
import org.jnp.server.Main;
import org.jnp.server.NamingServer;

public class JndiServer implements AutoCloseable {
    private NamingServer namingServer;
    private Main namingMain;
    private InitialContext context;

    static JndiServer startServer() throws Exception {
        final JndiServer util = new JndiServer();
        util.namingServer = new NamingServer();
        NamingContext.setLocal(util.namingServer);
        util.namingMain = new Main();
        util.namingMain.setInstallGlobalService(true);
        util.namingMain.setPort(-1);
        util.namingMain.start();
        return util;
    }

    JndiServer initializeNamingContext() throws NamingException  {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
        context = new InitialContext(props);
        
        return this;
    }

    JndiServer andBind(String jndiName, Object who) throws Exception {
        bind(jndiName, who, who.getClass(), context);
        return this;
    }

    
    /**
     * Helper method that binds the a non serializable object to the JNDI tree.
     *
     * @param jndiName Name under which the object must be bound
     * @param who Object to bind in JNDI
     * @param classType Class type under which should appear the bound object
     * @param ctx Naming context under which we bind the object
     * @throws Exception Thrown if a naming exception occurs during binding
     */
    private static void bind(String jndiName, Object who, Class classType, Context ctx) throws NamingException  {
        // Ah ! This service isn't serializable, so we use a helper class
        NonSerializableFactory.bind(jndiName, who);
        Name n = ctx.getNameParser("").parse(jndiName);
        while (n.size() > 1) {
            String ctxName = n.get(0);
            try {
                ctx = (Context) ctx.lookup(ctxName);
            } catch (NameNotFoundException e) {
                ctx = ctx.createSubcontext(ctxName);
            }
            n = n.getSuffix(1);
        }

        // The helper class NonSerializableFactory uses address type nns, we go on to
        // use the helper class to bind the service object in JNDI
        StringRefAddr addr = new StringRefAddr("nns", jndiName);
        Reference ref = new Reference(classType.getName(), addr, NonSerializableFactory.class.getName(), null);
        ctx.rebind(n.get(0), ref);
    }

    private static void unbind(String jndiName, Context ctx) throws NamingException {
        NonSerializableFactory.unbind(jndiName);
        ctx.unbind(jndiName);
    }

    @Override
    public void close() throws Exception {
        context.close();
        namingMain.stop();
    }
}

