package controllers;

import play.*;
import play.mvc.*;
import play.vfs.VirtualFile;
import java.util.*;
import java.io.*;

import org.apache.commons.io.IOUtils;

import java.sql.*;

import echosign.api.clientv11.ArrayOfString;
import echosign.api.clientv11.dto.*;
import echosign.api.clientv11.dto9.*;
import echosign.api.clientv11.dto11.*;
import echosign.api.clientv11.service.EchoSignDocumentService11Client;
import echosign.api.clientv11.service.EchoSignDocumentService11PortType;

public class Application extends Controller {

	private static String apiKey = Play.configuration.getProperty("echosign.apiKey");
	private static String url = Play.configuration.getProperty("echosign.url");
	private static String backUrl = Play.configuration.getProperty("echosign.backUrl");
	private static String fileName = Play.configuration.getProperty("echosign.fileName");
	private static EchoSignDocumentService11PortType cachedService;

	public static void index() {

		render();
	}

	public static void userName(){

		render();
	}

	public static void diffLogin(){

		render();
	}

	public static void mainLogin(){

		render();
	}

	public static void invalidLogin(){

		render();
	}

	public static void loginCheck(String uName, String uPassword){

		Connection conn = null;

		//To verify the login details based on the info in the database

		try
		{

			String userName = Play.configuration.getProperty("jdbc.userName");
			String password = Play.configuration.getProperty("jdbc.password");
			String jdbcUrl = Play.configuration.getProperty("jdbc.url");
			
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (jdbcUrl, userName, password);

			Statement s = conn.createStatement ();

			//if(uName.equals("admin") && uPassword.equals("aditya"))
			//	printDatabase();
			//else
			//{

			String query="select * from developers where userName=\""+uName+"\" and docKey=\""+uPassword+"\"";

			System.out.println(query);

			s.executeQuery(query); 

			ResultSet rs=s.getResultSet();

			if(rs.next())
				redirect("http://"+uName+":"+uPassword+"@code.mypuja.com/repos/frepo/branches/"+uName+"_"+uPassword);
			else
				invalidLogin();

			rs.close();
			//}
			s.close();
		}
		catch (Exception e)
		{
			//System.err.println ("Cannot connect to database server or insert values");
			e.printStackTrace();
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.close ();
				}
				catch (Exception e) { /* ignore close errors */ }
			}
		}

	}

	public static void sendPasswd() throws Exception
	{

		render();
	}

	public static void signNDA(){

		render();
	}

	public static void noSignUp(){
		
		render();
	}

	public static void forgotPasswd(){

		render();
	}

	public static void checkForgotPasswd(String uName, String mailId) throws Exception{

		Connection conn = null;

		try
		{

			//To verify the email entered in the case of forgotPasswd is already registered, signed the NDA

			String userName = Play.configuration.getProperty("jdbc.userName");
			String password = Play.configuration.getProperty("jdbc.password");
			String jdbcUrl = Play.configuration.getProperty("jdbc.url");
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (jdbcUrl, userName, password);

			Statement s = conn.createStatement ();

			String query="select docKey,docSignStatus from developers where userName=\""+uName+"\" and email=\""+mailId+"\"";

			s.executeQuery(query); 

			ResultSet rs=s.getResultSet();

			if(rs.next()){

				if(rs.getString("docSignStatus").equals("No"))
					signNDA();
				else
				{

					Process proc = Runtime.getRuntime().exec("/bin/sh sendMail.sh "+uName+" "+rs.getString("docKey")+" "+mailId);
					sendPasswd();
				}
			}
			else
				noSignUp();


			rs.close();
			s.close();
		}
		catch (Exception e)
		{
			//System.err.println ("Cannot connect to database server or insert values");
			e.printStackTrace();
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.close ();
				}
				catch (Exception e) { /* ignore close errors */ }
			}
		}
	
	}


	public static void sendDoc(String uName, String mailId) throws Exception{

		Connection conn = null;

		try
		{

			//Verify the userName for registration whether it has already been taken
			//If it hasn't been taken then mail him the NDA form

			String userName = Play.configuration.getProperty("jdbc.userName");
			String password = Play.configuration.getProperty("jdbc.password");
			String jdbcUrl = Play.configuration.getProperty("jdbc.url");
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (jdbcUrl, userName, password);

			Statement s = conn.createStatement ();

			String query="select userName from developers where userName=\""+uName+"\"";

			s.executeQuery(query); 

			ResultSet rs=s.getResultSet();


			if(rs.next())
				diffLogin();
			else
			{
				sendDocument(uName, mailId);

				rs.close();
				s.close();

				render();
			}
		}
		catch (Exception e)
		{
			//System.err.println ("Cannot connect to database server or insert values");
			e.printStackTrace();
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.close ();
				}
				catch (Exception e) { /* ignore close errors */ }
			}
		}
	
	}

	protected static EchoSignDocumentService11PortType getService(String url) {

		if (cachedService == null) {
			EchoSignDocumentService11Client client = new EchoSignDocumentService11Client();
			cachedService = client.getEchoSignDocumentService11HttpPort(url);
		}

		return cachedService;
	}

	protected static ArrayOfFileInfo getArrayOfFileInfos(String fileName) throws IOException {

		VirtualFile vf = VirtualFile.fromRelativePath("/app/"+fileName);
		File file = vf.getRealFile();        

		FileInfo fileInfo = new FileInfo();
		fileInfo.setFileName(file.getName());
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		IOUtils.copy(new FileInputStream(file), bytes);
		fileInfo.setFile(bytes.toByteArray());
		ArrayOfFileInfo fileInfos = new ArrayOfFileInfo();
		fileInfos.getFileInfo().add(fileInfo);

		return fileInfos;
	}

	protected static String sendDocument(String uName, String recipient) throws Exception
	{
		//Initialize the document to be sent over with the callBackInfo and some other things

		ArrayOfFileInfo fileInfos = getArrayOfFileInfos(fileName);

		ArrayOfString tos = new ArrayOfString();
		tos.getString().add(recipient);

		DocumentCreationInfo documentInfo = new DocumentCreationInfo();
		documentInfo.setTos(tos);
		documentInfo.setName("Document below");
		documentInfo.setMessage("Please sign this document as soon as possible");
		documentInfo.setFileInfos(fileInfos);
		documentInfo.setSignatureType(SignatureType.ESIGN);
		documentInfo.setSignatureFlow(SignatureFlow.SENDER_SIGNATURE_NOT_REQUIRED);

		CallbackInfo callInfo=new CallbackInfo();
		callInfo.setSignedDocumentUrl(backUrl);

		documentInfo.setCallbackInfo(callInfo);

		ArrayOfDocumentKey documentKeys = getService(url).sendDocument(apiKey, null, documentInfo);
		System.out.println("Document key is: " + documentKeys.getDocumentKey().get(0).getDocumentKey());

		Connection conn = null;

		try
		{

			//Update the database withe the info

			String userName = Play.configuration.getProperty("jdbc.userName");
			String password = Play.configuration.getProperty("jdbc.password");
			String jdbcUrl = Play.configuration.getProperty("jdbc.url");
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (jdbcUrl, userName, password);

			Statement s = conn.createStatement ();

			String query="INSERT INTO developers values (\""+documentKeys.getDocumentKey().get(0).getDocumentKey()+"\",\""+uName+"\",\""+recipient+"\",\"No\",null)";

			s.executeUpdate(query); 

			s.close();
		}
		catch (Exception e)
		{
			//System.err.println ("Cannot connect to database server or insert values");
			e.printStackTrace();
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.close ();
				}
				catch (Exception e) { /* ignore close errors */ }
			}
		}

		/*SigningUrlResult result=getService(url).getSigningUrl(apiKey,documentKeys.getDocumentKey().get(0).getDocumentKey());

		if(result.isSuccess())
			System.out.println("Success");
		else
			System.out.println(result.getErrorCode());

		return result.getSigningUrls().getSigningUrl().get(0).getEsignUrl();*/

		return documentKeys.getDocumentKey().get(0).getDocumentKey();
	}

	public static void createBranch(){

		//Now check which document has been signed

		//After finding it out, use the userInfo from the database to send over the SVN access details to the user

		Connection conn=null;

		try
		{

			String userName = Play.configuration.getProperty("jdbc.userName");
			String password = Play.configuration.getProperty("jdbc.password");
			String jdbcUrl = Play.configuration.getProperty("jdbc.url");
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (jdbcUrl, userName, password);

			Statement s = conn.createStatement ();

			s.executeQuery( "select docKey,userName,email from developers where docSignStatus=\"No\"");

			ResultSet rs=s.getResultSet();
			String temp=null;

			while(rs.next())
			{
				temp=rs.getString("docKey");

				DocumentInfo docInfo=getService(url).getDocumentInfo(apiKey, temp);

				if(docInfo.getStatus()== AgreementStatus.SIGNED)
				{
					Process proc = null;

					String command="sh /home/avinash/createBranch.sh "+rs.getString("userName")+" "+rs.getString("email")+" "+temp;
					System.out.println(command);

					proc = Runtime.getRuntime().exec(command);

					s.executeUpdate("update developers set docSignStatus=\"Yes\",svnBranchName=\""+rs.getString("userName")+"_"+rs.getString("docKey")+"\" where docKey=\""+temp+"\"");

				}

			}

			rs.close();
			s.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.close ();
				}
				catch (Exception e) { /* ignore close errors */ }
			}
		}

	}

}
