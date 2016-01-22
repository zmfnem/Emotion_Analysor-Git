package main;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Vector;
import java.io.IOException;
import java.util.ArrayList;

import ctrl.Analysor;


public class Emotion_analysor_server 
{
	public static void main(String[] args)
	{
		ServerSocket		server = null;
		Socket				socket = null;
		
		cEmotionResult		emotionresult = null;
		DetectorThread 		detector;
		
		// TODO:: Analysor init
		Analysor 			a = new Analysor();
		
		// TODO:: Server Socket init
		try 
		{
			server 			= new ServerSocket(10111);
			emotionresult	= new cEmotionResult();
			
			System.out.println("Server Ready...");
			while( true )
			{
				socket		= server.accept();
				detector	= new DetectorThread(socket, a, emotionresult);
				
				detector.start();
			}
		}
		catch(Exception e)
		{
			System.err.println("포트가 이미 열려있습니다.");
			System.exit(-1);
		}
	}
}


class DetectorThread extends Thread
{
	// TODO:: Socket
	private Socket			socket = null;
	private String			str = null;
	private BufferedReader	input = null;
	private PrintWriter		output = null;
	
	// TODO:: Analysor
	private Analysor		analysor;
	
	// TODO:: EmotionReslut
	private cEmotionResult	emotionresult;
	
	public DetectorThread( Socket _socket )
	{
		this.socket			= _socket;
		
		System.out.println("clint information" + this.socket.toString());
	}
	
	
	public DetectorThread(Socket _socket, cEmotionResult _mr)
	{
		this.socket			= _socket;
		this.emotionresult	= _mr;
		
		System.out.println("clint information " + this.socket.toString());
	}
	
	
	public DetectorThread(Socket _socket, Analysor _anl, cEmotionResult _mr)
	{
		this.socket			= _socket;
		this.analysor		= _anl;
		this.emotionresult	= _mr;
		
		System.out.println("clint information " + this.socket.toString());
	}
	
	
	public void run()
	{
		while( true )
		{
			try
			{
				input		= new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
				output		= new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ) );

				// 한글이 깨지지 않게 디코딩. URLDecoder.decode
				str = URLDecoder.decode(input.readLine(), "UTF-8");
				
				if( str == null ) 
					break;
				
				//str 		= dataParsing(str);
				
				System.out.println("STRING > " + str);
			}
			catch( Exception e )
			{
				System.out.println(e);
				break;
			}
			finally
			{
				stat();
				disconnectedtoclint();
			}
		}
		disconnectedtoclint();
	}
	
	
	public void disconnectedtoclint()
	{
		try 
		{
			if( socket.isClosed() == false)
			{
				System.out.println("clint down.");
				input.close();
				output.close();
				socket.close();
				emotionresult.resetResult();
			}
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public String dataParsing(String _str)
	{
		//TODO:: 넘어오는 URL에서 데이터만 추출.
		String				str = _str;
		
		str					= str.substring(11);
		str					= str.substring(0, str.length() - 9);
		
		return str;
	}
	
	
	public void stat()
	{
		if( str.equalsIgnoreCase("GET_RESULT") )
		{
			// TODO:: if clint want to result, then send a result.
			/////////////////////////////////////////////////////
			//totalProb[0] 토탈 긍부정
			//totalProb[1] angry
			//totalProb[2] happy
			//totalProb[3] love
			//totalProb[4] sad
			//this.analysor.getTotalProb()[0]
			/////////////////////////////////////////////////////
			
			ArrayList<ArrayList<String>>		arry = new ArrayList<ArrayList<String>>();
			double[] 		totalProb = new double[5];
			totalProb 		= this.analysor.getTotalProb();
			arry			= this.analysor.getTotalEmoWord();
			
			emotionresult.setTotal(Integer.toString( (int)(totalProb[0] * 100)) );
			emotionresult.setLoave(Integer.toString( (int)(totalProb[3]*100)) );
			emotionresult.setHappiness(Integer.toString( (int)(totalProb[2] * 100)) );
			emotionresult.setSadness(Integer.toString( (int)(totalProb[4] * 100)) );
			emotionresult.setAnger(Integer.toString( (int)(totalProb[1] * 100)) );
			
			int				count = 0;
			for( int i = 0; i < 4; i ++ )
			{
				count		= arry.get(i).size();
				for( int j = 0; j < arry.get(i).size(); j++ )
				{
					// MEMO:: 각 감정별로 키워드 3개씩.
					emotionresult.addKeywoards(arry.get(i).get(j));
				}
				
				for( int j = 0; j < 3-count; j++ )
					emotionresult.addKeywoards("NULL");
			}
			
			try 
			{
				String		temp;
				temp		= URLDecoder.decode(emotionresult.getEmotionResult(), "UTF-8");

				output.println(temp);
				output.flush();
			}
			catch (UnsupportedEncodingException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			// TODO:: Emotion analysor execute.
			try 
			{
				String		temp;
				temp		= URLDecoder.decode(str, "UTF-8");
				
				analysor.voiceAnalysis(temp);
				//analysor.textAnalysis("test.txt");
				
				output.println(temp);
				output.flush();
			} 
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}


class cEmotionResult
{
	String					mTotal;			
	String					mLove;			
	String					mHappiness;		
	String					mSadness;		
	String					mAnger;			
	Vector<String>			mvKeywords;		
	
	public void resetResult()
	{
		if( this.mvKeywords.isEmpty() == false )
			this.mvKeywords.clear();
	}
	
	public void setEmotionResult(String _lov, String _happ, String _sad, String _ang, String _tol)
	{
		this.mTotal			= _tol;
		this.mLove			= _lov;
		this.mHappiness		= _happ;
		this.mSadness		= _sad;
		this.mAnger			= _ang;
	}
	
	
	public void setTotal(String _tol)
	{
		this.mTotal			= _tol;
	}
	
	
	public void setLoave(String _lov)
	{
		this.mLove			= _lov;
	}
	
	
	public void setHappiness(String _happ)
	{
		this.mHappiness		= _happ;
	}
	
	
	public void setSadness(String _sad)
	{
		this.mSadness		= _sad;
	}
	
	
	public void setAnger(String _ang)
	{
		this.mAnger			= _ang;
	}
	
	
	public void addKeywoards(String _str)
	{
		if( this.mvKeywords == null )
			this.mvKeywords	= new Vector<String>();
		
		this.mvKeywords.add(_str);
		
		// Test Code..
		Iterator<String>	itr = this.mvKeywords.iterator();
		while( itr.hasNext() )
		{
			String			temp = itr.next();
			System.out.println(temp);
		}
	}
	
	
	public String getEmotionResult()
	{
		String				result;
		
		result				= "" + this.mLove;
		result				+= ":" + this.mHappiness;
		result				+= ":" + this.mSadness;
		result				+= ":" + this.mAnger;
		result				+= ":" + this.mTotal;
		
		Iterator<String>	itr = this.mvKeywords.iterator();
		while( itr.hasNext() )
		{
			String			temp = itr.next();
			result			+=":" + temp;
		}
		
		return result;
	}
}
