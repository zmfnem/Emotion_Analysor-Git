package com.example.avg.emotion_analysor_client;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements Runnable
{
    boolean                 state = false;
    int                     PORT = 10111;
    String                  IP = "220.117.126.60";

    // 결과 값들.
    String                  love_count;
    String                  happy_count;
    String                  sad_count;
    String                  anger_count;
    String                  total_count;

    String[]                love_str = new String[3];
    String[]                happy_str = new String[3];
    String[]                sad_str = new String[3];
    String[]                anger_str = new String[3];

    Socket                  sock;
    PrintWriter             output;
    BufferedReader          input;

    Thread                  t = new Thread(this);

    private final int       GOOGLE_STT = 1000;
    TextView                textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView            = (TextView)findViewById(R.id.result_text);

        // 핸드폰에 구글 STT 액티브가 있는지 확인, 없으면 버튼 비활성화.
        PackageManager      pm = getPackageManager();
        List<ResolveInfo>   activites = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

        if (activites.size() == 0)
        {
            Button          btr = (Button)findViewById(R.id.button);
            btr.setEnabled(false);
            Toast.makeText(this, "구글 STT 액티브가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Runnable recv_data_run = new Runnable()
    {
        @Override
        public void run()
        {
            textView.setText("서버연결 실패");
        }
    };

    Runnable recv_data_love = new Runnable()
    {
        @Override
        public void run()
        {
            ((TextView)findViewById(R.id.love)).setText(love_count + " - " + love_str[0] + ":" + love_str[1] + ":" + love_str[2]);
        }
    };

    Runnable recv_data_happy = new Runnable()
    {
        @Override
        public void run()
        {
            ((TextView)findViewById(R.id.happy)).setText(happy_count + " - " + happy_str[0] + ":" + happy_str[1] + ":" + happy_str[2]);
        }
    };

    Runnable recv_data_sad = new Runnable()
    {
        @Override
        public void run()
        {
            ((TextView)findViewById(R.id.sad)).setText(sad_count + " - " + sad_str[0] + ":" + sad_str[1] + ":" + sad_str[2]);
        }
    };

    Runnable recv_data_anger = new Runnable()
    {
        @Override
        public void run()
        {
            ((TextView)findViewById(R.id.anger)).setText(anger_count + " - " + anger_str[0] + ":" + anger_str[1] + ":" + anger_str[2]);
        }
    };

    Runnable recv_data_total = new Runnable()
    {
        @Override
        public void run()
        {
            ((TextView)findViewById(R.id.total)).setText(total_count);
        }
    };

    @Override
    public void run()
    {
        try
        {
            sock            = new Socket(IP, PORT);
            input		    = new BufferedReader( new InputStreamReader( sock.getInputStream() ) );
            output		    = new PrintWriter( new OutputStreamWriter( sock.getOutputStream() ) );

            /**
             *  GET_RESULT 메시지를 보낸다음, 서버로부터 결과를 받는다.*/
            if (state == true)
            {
                try
                {
                    output.println("GET_RESULT");
                    output.flush();

                    String read = input.readLine();
                    String  result = read;
                    String[]    resultSplit = result.split("[:]");

                    Log.d("output", result);

                    love_count  = resultSplit[0];
                    happy_count = resultSplit[1];
                    sad_count   = resultSplit[2];
                    anger_count = resultSplit[3];
                    total_count = resultSplit[4];

                    anger_str[0]    = resultSplit[5];
                    anger_str[1]    = resultSplit[6];
                    anger_str[2]    = resultSplit[7];

                    happy_str[0]    = resultSplit[8];
                    happy_str[1]    = resultSplit[9];
                    happy_str[2]    = resultSplit[10];

                    love_str[0] = resultSplit[11];
                    love_str[1] = resultSplit[12];
                    love_str[2] = resultSplit[13];

                    sad_str[0]  = resultSplit[14];
                    sad_str[1]  = resultSplit[15];
                    sad_str[2]  = resultSplit[16];

                    ((TextView)findViewById(R.id.love)).post(recv_data_love);
                    ((TextView)findViewById(R.id.happy)).post(recv_data_happy);
                    ((TextView)findViewById(R.id.sad)).post(recv_data_sad);
                    ((TextView)findViewById(R.id.anger)).post(recv_data_anger);
                    ((TextView)findViewById(R.id.total)).post(recv_data_total);

                    input.close();
                    output.close();
                    sock.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                t           = new Thread(this);
            }
        }
        catch( Exception e )
        {
            textView.post(recv_data_run);
        }
    }

    public void onClick(View view)
    {
        if (view.getId() == R.id.button)
        {
            try
            {
                // 버튼을 클릭하면 음성인식을 하기 위한 intent와 recognizer 설정.
                Intent      intent;
                intent      = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "말을 하세요");

                startActivityForResult(intent, GOOGLE_STT);

                Toast.makeText(this, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
                state       = false;
                t.start();
            }
            catch (ActivityNotFoundException e)
            {
                Toast.makeText(this, "음성인식 실패.", Toast.LENGTH_SHORT).show();
            }
        }
        else if (view.getId() == R.id.result_button)
        {
            state           = true;
            t.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_STT && resultCode == RESULT_OK)
        {
            String          str = "";
            ArrayList<String>   results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            str             = results.get(0).toString();
            textView.setText("" + str);

            try
            {
                output.println(str);
                output.flush();

                input.close();
                output.close();
                sock.close();
                t           = new Thread(this);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
