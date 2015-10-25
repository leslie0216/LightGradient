package usask.chl848.lightgradient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

/**
 * View
 */
public class MainView extends View {
    Paint m_paint;

    private static final int m_textSize = 70;
    private static final int m_textStrokeWidth = 2;
    private static final int m_boundaryStrokeWidth = 10;

    private String m_name;
    private String m_username;
    private int m_color;
    private double m_angle;
    private String m_message;
    private Point m_minLoc;
    private Point m_maxLoc;
    private int m_imgWidth;
    private int m_imgHeight;

    private ArrayList<Ball> m_balls;
    private int m_touchedBallId;

    private class Ball {
        public int m_ballColor;
        public float m_ballX;
        public float m_ballY;
        public boolean m_isTouched;
        public String m_id;
    }

    private float m_ballRadius;
    private float m_ballBornX;
    private float m_ballBornY;

    private float m_localCoordinateCenterX;
    private float m_localCoordinateCenterY;
    private float m_localCoordinateRadius;

    public class RemotePhoneInfo {
        String m_name;
        int m_color;
        float m_angle;
    }

    private ArrayList<RemotePhoneInfo> m_remotePhones;
    private float m_remotePhoneRadius;

    private boolean m_showRemoteNames;
    final Handler handler = new Handler();
    Runnable mLongPressed = new Runnable() {
        @Override
        public void run() {
            setShowRemoteNames(true);
            invalidate();
        }
    };

    private MainLogger m_logger = null;
    private boolean m_logEnabled;
    private int m_logCount;

    public MainView(Context context) {
        super(context);

        m_paint = new Paint();

        m_minLoc = new Point(0.0,0.0);
        m_maxLoc = new Point(0.0,0.0);
        m_imgWidth = 0;
        m_imgHeight = 0;

        m_touchedBallId = -1;
        m_balls = new ArrayList<>();

        m_remotePhones = new ArrayList<>();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        initBallBornPoints(displayMetrics);

        m_message = "No Message";

        m_username = ((MainActivity)(context)).getUserName();
        m_color = ((MainActivity)(context)).getUserColor();

        m_localCoordinateCenterX = displayMetrics.widthPixels * 0.5f;
        m_localCoordinateCenterY = displayMetrics.heightPixels * 0.9f;
        m_localCoordinateRadius = displayMetrics.widthPixels * 0.5f;

        m_remotePhoneRadius = displayMetrics.heightPixels * 0.05f;

        setShowRemoteNames(false);

        m_logger = new MainLogger(getContext(), m_username+"_"+getResources().getString(R.string.app_name)+"_angle");
        m_logger.writeHeaders("userName" + ","  + "angle" + "," + "timestamp");
        m_logEnabled = false;
        m_logCount = 0;
    }

    public void enableLog()
    {
        m_logEnabled = true;
    }

    public void disableLog()
    {
        if (isLogEnabled())
        {
            m_logger.flush();
        }
        m_logEnabled = false;
    }

    public boolean isLogEnabled()
    {
        return m_logEnabled;
    }

    private void initBallBornPoints(DisplayMetrics displayMetrics) {
        m_ballRadius = displayMetrics.heightPixels * 0.08f;
        m_ballBornX = displayMetrics.widthPixels * 0.5f;
        m_ballBornY = displayMetrics.heightPixels * 0.9f - m_ballRadius * 2.0f;
    }

    private void setShowRemoteNames(boolean show) {
        m_showRemoteNames = show;
    }

    private boolean getShowRemoteNames() {
        return m_showRemoteNames;
    }

    public void setDeviceName(String name) {
        m_name = name;
    }

    public String getDeviceName() {
        return m_name;
    }

    public void setAngle(double angle) {
        m_angle = angle;
    }

    public void setMessage(String message) {
        m_message = message;
    }

    public void setLoc(Point minLoc, Point maxLoc, int imgWidth, int imgHeight){
        m_minLoc = minLoc;
        m_maxLoc = maxLoc;
        m_imgWidth = imgWidth;
        m_imgHeight = imgHeight;
        calculateAngle();
    }

    private void calculateAngle() {
        m_angle = Math.toDegrees(Math.atan2((m_minLoc.x - m_maxLoc.x), (m_minLoc.y - m_maxLoc.y)));
        if (isLogEnabled())
        {
            m_logger.write(m_username + "," + m_angle + "," + System.currentTimeMillis(), true);
            ++m_logCount;
        }
    }


    private double calculateRemoteAngle(double raw_angle) {
        double angle_2 = (m_angle - raw_angle)/2.0f;
        if (angle_2 < 0){
            angle_2 = 180.0f+angle_2;
        }

        return angle_2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        showLocalAngle(canvas);
        showMessage(canvas);
        showDirection(canvas);

        showLocalCircleCoordinate(canvas);
        showBalls(canvas);
        showBoundary(canvas);
        showLogCount(canvas);
    }

    private void showLogCount(Canvas canvas) {
        m_paint.setTextSize(m_textSize);
        m_paint.setColor(Color.BLUE);
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        m_paint.setStrokeWidth(m_textStrokeWidth);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        canvas.drawText("Count : " + m_logCount, displayMetrics.widthPixels * 0.75f, displayMetrics.heightPixels * 0.1f, m_paint);
    }

    private void showLocalAngle(Canvas canvas) {
        m_paint.setTextSize(m_textSize);
        m_paint.setColor(Color.RED);
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        m_paint.setStrokeWidth(m_textStrokeWidth);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        canvas.drawText("angle : " + String.format("%.4f", m_angle), displayMetrics.widthPixels * 0.1f, displayMetrics.heightPixels * 0.95f, m_paint);
    }

    private void showMessage(Canvas canvas) {
        m_paint.setTextSize(m_textSize);
        m_paint.setColor(Color.GREEN);
        m_paint.setStrokeWidth(m_textStrokeWidth);
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        canvas.drawText(m_message, displayMetrics.widthPixels * 0.4f, displayMetrics.heightPixels * 0.95f, m_paint);
    }

    private void showDirection(Canvas canvas) {
        if (m_imgWidth != 0 && m_imgHeight != 0) {
            m_paint.setColor(Color.RED);
            m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
            m_paint.setStrokeWidth(m_textStrokeWidth);

            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            double maxX = (m_maxLoc.x/m_imgWidth) * displayMetrics.widthPixels;
            double maxY = (m_maxLoc.y/m_imgHeight) * displayMetrics.heightPixels;
            canvas.drawCircle((float)maxX, (float)maxY, 30, m_paint);

            double minX = (m_minLoc.x/m_imgWidth) * displayMetrics.widthPixels;
            double minY = (m_minLoc.y/m_imgHeight) * displayMetrics.heightPixels;
            canvas.drawLine((float)maxX, (float)maxY, (float)minX, (float)minY, m_paint);
        }
    }

    private void showLocalCircleCoordinate(Canvas canvas){
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

        float crosshairX = displayMetrics.widthPixels * 0.5f;
        float crosshairY = displayMetrics.heightPixels * 0.5f;
        float crosshairRadius = 50.0f;

        m_paint.setColor(Color.BLUE);
        m_paint.setStyle(Paint.Style.STROKE);

        // draw crosshair
        //canvas.drawCircle(crosshairX, crosshairY, crosshairRadius, m_paint);
        //canvas.drawLine(crosshairX - crosshairRadius * 2, crosshairY, crosshairX + crosshairRadius * 2, crosshairY, m_paint);
        //canvas.drawLine(crosshairX, crosshairY - crosshairRadius * 2, crosshairX, crosshairY + crosshairRadius * 2, m_paint);

        // draw coordinate
        float left = 0.0f;
        float top = displayMetrics.heightPixels * 0.9f - m_localCoordinateRadius;
        float right = displayMetrics.widthPixels;
        float bottom = displayMetrics.heightPixels * 0.9f + m_localCoordinateRadius;
        RectF disRect = new RectF(left, top, right, bottom);

        m_paint.setStrokeWidth(m_boundaryStrokeWidth);
        canvas.drawArc(disRect, 180.0f, 180.0f, false, m_paint);

        MainActivity mainActivity = (MainActivity)getContext();
        if ((mainActivity != null) && mainActivity.m_bluetoothData.isConnected()) {
            showRemotePhones(canvas);
        }
    }

    private void showRemotePhones(Canvas canvas) {
        if (!m_remotePhones.isEmpty()) {
            int size = m_remotePhones.size();
            for (int i=0; i<size; ++i) {
                RemotePhoneInfo info = m_remotePhones.get(i);
                double angle_remote = calculateRemoteAngle(info.m_angle);
                float pointX = m_localCoordinateCenterX + m_localCoordinateRadius * (float)Math.cos(Math.toRadians(angle_remote));
                float pointY = m_localCoordinateCenterY - m_localCoordinateRadius * (float)Math.sin(Math.toRadians(angle_remote));
                m_paint.setColor(info.m_color);
                m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawCircle(pointX, pointY, m_remotePhoneRadius, m_paint);

                if (getShowRemoteNames()) {
                    m_paint.setTextSize(m_textSize);
                    m_paint.setStrokeWidth(m_textStrokeWidth);
                    float textX = pointX - m_remotePhoneRadius;
                    float textY = pointY - m_remotePhoneRadius * 1.5f;
                    if (info.m_name.length() > 5) {
                        textX = pointX - m_remotePhoneRadius * 2.0f;
                    }
                    canvas.drawText(info.m_name, textX, textY, m_paint);
                }
            }
        }
    }

    private void showBalls(Canvas canvas) {
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);
        for (Ball ball : m_balls) {
            m_paint.setColor(ball.m_ballColor);
            canvas.drawCircle(ball.m_ballX, ball.m_ballY, m_ballRadius, m_paint);
        }
    }

    private void showBoundary(Canvas canvas) {
        m_paint.setColor(Color.RED);
        m_paint.setStrokeWidth(m_boundaryStrokeWidth);
        m_paint.setStyle(Paint.Style.FILL_AND_STROKE);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        canvas.drawLine(0, displayMetrics.heightPixels * 0.9f, displayMetrics.widthPixels, displayMetrics.heightPixels * 0.9f, m_paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float X = event.getX();
        float Y = event.getY();
        float touchRadius = event.getTouchMajor();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                m_touchedBallId = -1;
                for (int i = 0; i < m_balls.size(); ++i) {
                    Ball ball = m_balls.get(i);
                    ball.m_isTouched = false;

                    double dist;
                    dist = Math.sqrt(Math.pow((X - ball.m_ballX), 2) + Math.pow((Y - ball.m_ballY), 2));
                    if (dist <= (touchRadius + m_ballRadius)) {
                        ball.m_isTouched = true;
                        m_touchedBallId = i;

                        boolean isOverlap = false;
                        for (int j = 0; j < m_balls.size(); ++j) {
                            if (j != m_touchedBallId) {
                                Ball ball2 = m_balls.get(j);

                                double dist2 = Math.sqrt(Math.pow((X - ball2.m_ballX), 2) + Math.pow((Y - ball2.m_ballY), 2));
                                if (dist2 <= m_ballRadius * 2) {
                                    isOverlap = true;
                                }
                            }
                        }

                        if (!isOverlap && !isBoundary(X, Y)) {
                            ball.m_ballX = X;
                            ball.m_ballY = Y;
                            this.invalidate();
                        }
                    }

                    if (m_touchedBallId > -1) {
                        break;
                    }
                }

                if (m_touchedBallId == -1) {

                    boolean show = false;

                    for (RemotePhoneInfo remotePhone : m_remotePhones) {
                        double angle_remote = calculateRemoteAngle(remotePhone.m_angle);
                        float pointX = m_localCoordinateCenterX + m_localCoordinateRadius * (float) Math.cos(Math.toRadians(angle_remote));
                        float pointY = m_localCoordinateCenterY - m_localCoordinateRadius * (float) Math.sin(Math.toRadians(angle_remote));

                        double dist = Math.sqrt(Math.pow((X - pointX), 2) + Math.pow((Y - pointY), 2));

                        if (dist <= (touchRadius + m_remotePhoneRadius)) {
                            show = true;
                            break;
                        }
                    }

                    if (show) {
                        handler.postDelayed(mLongPressed, 500);
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (getShowRemoteNames()) {
                    boolean show = false;

                    for (RemotePhoneInfo remotePhone : m_remotePhones) {
                        double angle_remote = calculateRemoteAngle(remotePhone.m_angle);
                        float pointX = m_localCoordinateCenterX + m_localCoordinateRadius * (float) Math.cos(Math.toRadians(angle_remote));
                        float pointY = m_localCoordinateCenterY - m_localCoordinateRadius * (float) Math.sin(Math.toRadians(angle_remote));

                        double dist = Math.sqrt(Math.pow((X - pointX), 2) + Math.pow((Y - pointY), 2));

                        if (dist <= (touchRadius + m_remotePhoneRadius)) {
                            show = true;
                            break;
                        }
                    }

                    if (!show) {
                        handler.removeCallbacks(mLongPressed);
                        setShowRemoteNames(false);
                        invalidate();
                    }
                }

                if (m_touchedBallId > -1) {
                    Ball ball = m_balls.get(m_touchedBallId);
                    if (ball.m_isTouched) {
                        boolean isOverlap = false;

                        for (int j = 0; j < m_balls.size(); ++j) {
                            if (j != m_touchedBallId) {
                                Ball ball2 = m_balls.get(j);

                                double dist = Math.sqrt(Math.pow((X - ball2.m_ballX), 2) + Math.pow((Y - ball2.m_ballY), 2));
                                if (dist <= m_ballRadius * 2) {
                                    isOverlap = true;
                                }
                            }
                        }

                        if (!isOverlap & !isBoundary(X, Y)) {
                            ball.m_ballX = X;
                            ball.m_ballY = Y;
                            this.invalidate();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(mLongPressed);
                if (getShowRemoteNames()) {
                    setShowRemoteNames(false);
                    invalidate();
                }

                if (m_touchedBallId > -1) {
                    Ball ball = m_balls.get(m_touchedBallId);
                    if (ball.m_isTouched) {
                        boolean isOverlap = false;

                        for (int j = 0; j < m_balls.size(); ++j) {
                            if (j != m_touchedBallId) {
                                Ball ball2 = m_balls.get(j);

                                double dist = Math.sqrt(Math.pow((X - ball2.m_ballX), 2) + Math.pow((Y - ball2.m_ballY), 2));
                                if (dist <= m_ballRadius * 2) {
                                    isOverlap = true;
                                }
                            }
                        }

                        if (!isOverlap) {
                            String name = isSending(ball.m_ballX, ball.m_ballY);
                            if (!name.isEmpty()) {
                                ((MainActivity) getContext()).showToast("send ball to : " + name);
                                sendBall(ball, name);
                                removeBall(ball.m_id);
                                this.invalidate();
                            }
                        }
                    }
                }

                for (Ball ball : m_balls) {
                    ball.m_isTouched = false;
                }
                break;
        }
        return true;
    }

    private boolean isBoundary(float x, float y) {
        boolean rt = false;
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();

        while (true) {
            // check bottom
            if ((y + m_ballRadius) >= (displayMetrics.heightPixels * 0.9f)) {
                rt = true;
                break;
            }

            // check left
            if (x - m_ballRadius <= 0.0f) {
                rt = true;
                break;
            }

            // check right
            if (x + m_ballRadius >= displayMetrics.widthPixels) {
                rt = true;
                break;
            }

            //check top
            double dist = Math.sqrt(Math.pow((x - m_localCoordinateCenterX), 2) + Math.pow((y - m_localCoordinateCenterY), 2));
            if (dist + m_ballRadius >= m_localCoordinateRadius) {
                rt = true;
            }
            break;
        }

        return rt;
    }

    private String isSending(float x, float y) {
        String receiverName = "";
        float rate = 10000.0f;
        if (!m_remotePhones.isEmpty()) {
            for (RemotePhoneInfo remotePhoneInfo : m_remotePhones) {
                double angle_remote = calculateRemoteAngle(remotePhoneInfo.m_angle);
                float pointX = m_localCoordinateCenterX + m_localCoordinateRadius * (float)Math.cos(Math.toRadians(angle_remote));
                float pointY = m_localCoordinateCenterY - m_localCoordinateRadius * (float)Math.sin(Math.toRadians(angle_remote));

                double dist = Math.sqrt(Math.pow((x - pointX), 2) + Math.pow((y - pointY), 2));
                if (dist < (m_remotePhoneRadius + m_ballRadius)){
                    if (dist < rate) {
                        receiverName = remotePhoneInfo.m_name;
                        rate = (float)dist;
                    }
                }
            }
        }

        return receiverName;
    }

    public void addBall() {
        Ball ball = new Ball();
        Random rnd = new Random();
        ball.m_ballColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        ball.m_ballX = m_ballBornX;
        ball.m_ballY = m_ballBornY;
        ball.m_isTouched = false;
        ball.m_id = UUID.randomUUID().toString();
        m_balls.add(ball);
    }

    public  void removeBall(String id) {
        for (Ball ball : m_balls) {
            if (ball.m_id.equalsIgnoreCase(id)) {
                m_balls.remove(ball);
                m_touchedBallId = -1;
                break;
            }
        }
    }

    public void receivedBall(String id, int color) {
        boolean isReceived = false;
        for (Ball ball : m_balls) {
            if (ball.m_id.equalsIgnoreCase(id)) {
                isReceived = true;
                break;
            }
        }

        if (!isReceived) {
            Ball ball = new Ball();
            ball.m_id = id;
            ball.m_ballColor = color;
            ball.m_isTouched = false;

            ball.m_ballX = m_ballBornX;
            ball.m_ballY = m_ballBornY;

            m_balls.add(ball);
        }
    }

    public void sendBall(Ball ball, String receiverName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("x", 0.0f);
            jsonObject.put("y", 0.0f);
            jsonObject.put("z", m_angle);
            jsonObject.put("color", m_color);
            jsonObject.put("name", m_name);
            jsonObject.put("isSendingBall", true);
            jsonObject.put("ballId", ball.m_id);
            jsonObject.put("ballColor", ball.m_ballColor);
            jsonObject.put("receiverName", receiverName);
        } catch (JSONException e){
            e.printStackTrace();
        }

        MainActivity ca = (MainActivity)getContext();
        if (ca != null) {
            ca.m_bluetoothData.addMessage(jsonObject.toString());
        }
    }

    public void sendLocation(){
        JSONObject msg = new JSONObject();
        try {
            msg.put("x", 0.0f);
            msg.put("y", 0.0f);
            msg.put("z", m_angle);
            msg.put("name", m_name);
            msg.put("color", m_color);
            msg.put("isSendingBall", false);
        } catch (JSONException e){
            e.printStackTrace();
        }

        MainActivity ca = (MainActivity)getContext();
        if (ca != null) {
            ca.m_bluetoothData.addMessage(msg.toString());
        }
    }

    public void updateRemotePhone(String name, int color, float angle){
        if (name.isEmpty() || name.equalsIgnoreCase(m_name)) {
            return;
        }

        int size = m_remotePhones.size();
        boolean isFound = false;
        for (int i = 0; i<size; ++i) {
            RemotePhoneInfo info = m_remotePhones.get(i);
            if (info.m_name.equalsIgnoreCase(name)) {
                info.m_color = color;
                info.m_angle = angle;
                isFound = true;
                break;
            }
        }

        if (!isFound) {
            RemotePhoneInfo info = new RemotePhoneInfo();
            info.m_name = name;
            info.m_color = color;
            info.m_angle = angle;
            m_remotePhones.add(info);
        }
    }

    public ArrayList<RemotePhoneInfo> getRemotePhones() {
        return m_remotePhones;
    }

    public void removePhones(ArrayList<RemotePhoneInfo> phoneInfos) {
        m_remotePhones.removeAll(phoneInfos);
    }

    public void clearRemotePhoneInfo() {
        m_remotePhones.clear();
    }

    public int getBallCount() {
        return m_balls.size();
    }

}
