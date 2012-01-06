import java.net.ServerSocket;
import java.io.*;
import java.sql.*;

public class FPort{

	public void start(String dirName, String user, String repo, String rev){

		ServerSocket socket = null;
		int portNumber=-1;
		String mailId=null;
		String docKey=null;

		try {
			socket = new ServerSocket(0);

			if (socket != null) 
			{
				//To find the next available non-privileged port

				portNumber=socket.getLocalPort();
				socket.close(); 

				Connection conn=null;
				String userName = "baggio";
				String password = "thummala";
				String url = "jdbc:mysql://localhost:5123/avinash";
				Class.forName ("com.mysql.jdbc.Driver").newInstance ();
				conn = DriverManager.getConnection (url, userName, password);

				Statement s = conn.createStatement ();
				s.executeQuery( "Select email,docKey from developers where userName=\""+user+"\"");

				ResultSet rs=s.getResultSet();

				if(rs.next())
				{
					//Run the shell script to deploy the play app on the new port.
					//Also notify the user about the deployment details

					String command="/bin/sh mPlay.sh "+portNumber+" "+dirName+" "+rs.getString("email")+" "+user+" "+rs.getString("docKey")+" "+repo+" "+rev;
					Process proc = null;

					proc = Runtime.getRuntime().exec(command);
				}


				rs.close();
				s.close();
				conn.close();

			}


		}

		catch (Exception e) {

			e.printStackTrace();
		} 


	}

	public static void main(String[] args)
	{
		FPort findPort=new FPort();

		findPort.start(args[0], args[1], args[2], args[3]);
	}

}


