import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import static java.rmi.server.RemoteServer.getClientHost;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.List;

public class RemoteService  extends UnicastRemoteObject
        implements RemoteServiceInterface
{
    public static final String SERVICE_NAME = "GetRemoteFile";
    List<RemoteObserverInterface> observers;
    List<RemoteClientInterface> servers;
    RemoteMonitoringInterface monitoring;
    
    public RemoteService(File localDirectory) throws RemoteException 
    {
        observers = new ArrayList<>();
        servers = new ArrayList<>();
        monitoring = null;
    }
    
    @Override
    public void addServer(RemoteClientInterface server) throws java.rmi.RemoteException
    {
        try
        {
            notifyObservers("O servidor " + server.getName() +
                    " conectou-se desde " + getClientHost());
            servers.add(server);
            if(monitoring != null)
            {
                monitoring.notifyNewServer(server);
            }
        }
        catch (ServerNotActiveException ex) { }
    }
    
    @Override
    public void removeServer(RemoteClientInterface server) throws java.rmi.RemoteException
    {
        if(servers.remove(server))
        {
            notifyObservers("O servidor " + server.getName() + " desconectou-se!");
            if(monitoring != null)
            {
                monitoring.notifyCloseServer(server);
            }
        }
    }
    
    @Override
    public synchronized void addObserver(RemoteObserverInterface observer)
            throws java.rmi.RemoteException
    {
        if(!observers.contains(observer))
        {
            observers.add(observer);
            System.out.println("+ um observador.");
        }

    }
    
    @Override
    public synchronized void removeObserver(RemoteObserverInterface observer) throws java.rmi.RemoteException
    {
        if(observers.remove(observer))
            System.out.println("- um observador.");
    }
    
    public synchronized void notifyObservers(String msg)
    {
        int i;
        
        for(i=0; i < observers.size(); i++)
        {
            try
            {       
                observers.get(i).notifyNewOperationConcluded(msg);
            }
            catch(RemoteException e)
            {
                observers.remove(i--);
                System.out.println("- um observador (observador inacessivel).");
            }
        }
    }

    @Override
    public List<RemoteClientInterface> allServersInfo() throws RemoteException
    {
        int i;
        List<RemoteClientInterface> values = new ArrayList<>();
        for(i=0; i < servers.size(); i++)
        {
            try
            {       
                servers.get(i).getName();
                values.add(servers.get(i));
            }
            catch(RemoteException e)
            {
                observers.remove(i--);
                System.out.println("Um servidor desconectou-se (Inacessivel).");
            }
        }
        return values;
    }

    @Override
    public RemoteClientInterface searchServerByName(String serverName)
            throws RemoteException
    {
        for(RemoteClientInterface server : servers)
        {
            if(server.getName().equals(serverName))
            {
                return server;
            }
        }
        throw new RemoteException();
    }
    
    @Override
    public List<String> getAllObserversName()
            throws RemoteException
    {
        List<String> observers_name;
        List<RemoteObserverInterface> tmp;
        observers_name = new ArrayList<>();
        
        for(RemoteClientInterface server : servers)
        {
            tmp = server.getAllConnectedUsers();
            for(RemoteObserverInterface observer : tmp)
            {
                if(!observers_name.contains(observer.getName()))
                {
                    observers_name.add(observer.getName());
                }
            }
        }
        
        return observers_name;
    }

    @Override
    public void setMonitoringApp(RemoteMonitoringInterface monitoringApp)
            throws RemoteException
    {
        monitoring = monitoringApp;
    }

    @Override
    public void loginUser(RemoteObserverInterface user, RemoteClientInterface server)
            throws RemoteException
    {
        if(monitoring != null)
        {
            monitoring.notifyNewUser(user, server);
        }
    }

    @Override
    public void logoutUser(RemoteObserverInterface user, RemoteClientInterface server)
            throws RemoteException
    {
        if(monitoring != null)
        {
            monitoring.notifyCloseUser(user, server);
        }
    }
}
