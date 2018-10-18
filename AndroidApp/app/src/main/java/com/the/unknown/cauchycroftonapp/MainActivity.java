package com.the.unknown.cauchycroftonapp;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static java.lang.Math.abs;


public class MainActivity extends AppCompatActivity {

    private android.support.v7.widget.Toolbar mToolBar;

    private ImageView mImageView;

    private Button mPickImgBt;
    private Switch mSwitchImage;
    private Switch mSwitchCurve;
    private Switch mSwitchBackground;

    private SeekBar mSeekWidth;
    private TextView mViewWidth;
    private ImageButton mDefaultWidth;

    private Button mCurvePointBt;
    private SeekBar mSeekTolerance;

    private SeekBar mSeekNumberLines;
    private TextView mViewNumberLines;
    private SeekBar mSeekAngleLines;
    private TextView mViewAngleLines;
    private Switch mSwitchLines;

    private RadioButton mRadioAddDots;
    private RadioButton mRadioNone;
    private RadioButton mRadioSupprDots;
    private ImageButton mSupprAllDots;
    private ImageButton mAutoDots;
    private Switch mSwitchDots;

    private Switch mSwitchRef;
    private Button mRefBeginBt;
    private Button mRefEndBt;
    private EditText mEditRefLenght;
    private EditText mEditRefUnit;
    private ImageButton mKeyboardDown;

    private TextView mLengthView;





    private Bitmap mPicture;
    private Bitmap mCurve;
    private Bitmap mLines;
    private Canvas mLinesCanvas;
    private Bitmap mDots;
    private Canvas mDotsCanvas;
    private Bitmap mRef;
    private Canvas mRefCanvas;

    private Uri mSelectedImage;


    private int mBmpWidth;
    private int mBmpHeight;
    private static final int DEFAULT_WIDTH = 480;
    private static final int MIN_WIDTH = 20;

    private int STATE;
    private static final int NONE = 0;
    private static final int SELECT_CURVE = 1;
    private static final int SELECT_REF1 = 2;
    private static final int SELECT_REF2 = 3;
    private static final int ADD_DOT      = 4;
    private static final int SUPPR_DOT    = 5;

    private static final int CURVE_COLOR = Color.parseColor("#1976D2");
    private static final int CURVE_POINT_COLOR = Color.parseColor("#0D47A1");
    private static final int LINES_COLOR = Color.parseColor("#388E3C");
    private static final int DOTS_COLOR  = Color.parseColor("#D32F2F");
    private static final int REF_LINE_COLOR   = Color.parseColor("#455A64");
    private static final int REF_COLOR   = Color.parseColor("#263238");

    public static final double PI = 3.141592653589793238462643383;

    private int mCurveColor;
    private int mCurvePointX;
    private int mCurvePointY;
    private boolean mCurvePointSelected;
    private boolean mImageSelected;
    private int mLinesNumber;
    private int mLinesAngle;
    private List<Point> mDotsList;
    private Point mRefBegin;
    private Point mRefEnd;
    private double mRefDist;


    public static final int PICK_IMAGE = 1;
    public static final int TOL = 2;


    private void setCurvePoint(int x, int y){
        if(mImageSelected){
            mCurveColor = mPicture.getPixel(x, y);
            mCurvePointSelected = true;
            mCurvePointX = x;
            mCurvePointY = y;
        }
    }
    private void setCurve(){
        setCurveWithBackground(Color.TRANSPARENT);
    }
    private void setCurveWithBackground(int bgColor){
        if(mImageSelected && mCurvePointSelected){
            int[] intbmp = new int[mBmpWidth*mBmpHeight];
            int[] intcurve = new int[mBmpWidth*mBmpHeight];
            mPicture.getPixels(intbmp, 0, mBmpWidth, 0, 0, mBmpWidth, mBmpHeight);
            final int TOLERANCE = (int) Math.pow(TOL, mSeekTolerance.getProgress());
            for(int i=0; i<mBmpWidth; i++){
                for(int j=0; j<mBmpHeight; j++){
                    if( abs(intbmp[j*mBmpWidth+i]-mCurveColor)<TOLERANCE ){
                        intcurve[j*mBmpWidth+i] = CURVE_COLOR;
                    } else {
                        intcurve[j*mBmpWidth+i] = bgColor;
                    }
                }
            }
            mCurve.setPixels(intcurve, 0, mBmpWidth, 0, 0, mBmpWidth, mBmpHeight);
            Canvas can = new Canvas(mCurve);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            can.drawCircle(mCurvePointX, mCurvePointY, 4, paint);
            paint.setColor(CURVE_POINT_COLOR);
            can.drawCircle(mCurvePointX, mCurvePointY, 3, paint);
        }
    }
    private void setLines(){
        mLines = Bitmap.createBitmap(mBmpWidth, mBmpHeight, Bitmap.Config.ARGB_8888);
        mLinesCanvas = new Canvas(mLines);
        Paint paint = new Paint();
        paint.setColor(LINES_COLOR);
        int d = mBmpWidth/mLinesNumber;
        double angle = PI/mLinesAngle;

        //main drawing loop
        for(int j=0; j<mLinesAngle; j++){
            //avoid vertical lines
            if(mLinesAngle==j*2){
                //draw vertical lines if angle>=1 (anytime)
                for(int i=0; i<mLinesNumber; i++){
                    mLinesCanvas.drawLine((d/2)+d*i, 0, d/2+d*i, mBmpHeight, paint);
                }
            } else{
                double theta = j*angle;
                int a = (int) (d/Math.cos(theta));
                int h = (int) (Math.tan(theta)*mBmpWidth);
                if(a>0){
                    int yStart = d/2-a;
                    int yEnd = yStart-h;
                    while(yEnd<mBmpHeight){
                        yStart += a;
                        yEnd += a;
                        mLinesCanvas.drawLine(0, yStart, mBmpWidth, yEnd, paint);
                    }
                }else {
                    int yStart = mBmpHeight-d/2-a;
                    int yEnd = yStart-h;
                    while(yEnd>0){
                        yStart += a;
                        yEnd += a;
                        mLinesCanvas.drawLine(0, yStart, mBmpWidth, yEnd, paint);
                    }
                }
            }
        }
    }
    private void drawDots(){
        mDots = Bitmap.createBitmap(mBmpWidth, mBmpHeight, Bitmap.Config.ARGB_8888);
        mDotsCanvas = new Canvas(mDots);
        Paint paint = new Paint();
        paint.setColor(DOTS_COLOR);
        Point point;
        for (Point aMDotsList : mDotsList) {
            point = aMDotsList;
            drawDot(point);
        }
    }
    private void drawDot(Point point){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        mDotsCanvas.drawCircle(point.mx, point.my, 4, paint);
        paint.setColor(DOTS_COLOR);
        mDotsCanvas.drawCircle(point.mx, point.my, 3, paint);
    }
    private void addDot(int x, int y){
        Point point = new Point(x,y);
        mDotsList.add(point);
        drawDot(point);
    }
    private void supprDot(int x, int y ){
        if(!mDotsList.isEmpty()){
            Point point = new Point(x,y);
            int i=0;
            int dist = point.squareDistTo(mDotsList.get(0));
            ListIterator<Point> it = mDotsList.listIterator(1);
            while (it.hasNext()){
                int d = point.squareDistTo(it.next());
                if(d<dist){
                    i=it.previousIndex();
                    dist = d;
                }
            }
            mDotsList.remove(i);
            drawDots();
        }
    }
    private void supprDots(){
        mDotsList.clear();
        mDots = Bitmap.createBitmap(mBmpWidth, mBmpHeight, Bitmap.Config.ARGB_8888);
        mDotsCanvas = new Canvas(mDots);
    }
    private int minDist(int x, int y ){
        if(!mDotsList.isEmpty()){
            Point point = new Point(x,y);
            int dist = point.squareDistTo(mDotsList.get(0));
            ListIterator<Point> it = mDotsList.listIterator(1);
            while (it.hasNext()){
                int d = point.squareDistTo(it.next());
                if(d<dist){
                    it.previousIndex();
                    dist = d;
                }
            }
            return dist;
        }
        else {
            return mBmpHeight+mBmpWidth;
        }
    }
    private void calcDots(){
        if(mCurvePointSelected){
            mDotsList.clear();

            int[] curve = new int[mBmpWidth*mBmpHeight];
            int[] lines = new int[mBmpWidth*mBmpHeight];
            mCurve.getPixels(curve, 0, mBmpWidth, 0, 0, mBmpWidth, mBmpHeight);
            mLines.getPixels(lines, 0, mBmpWidth, 0, 0, mBmpWidth, mBmpHeight);

            for(int i=0; i<mBmpWidth; i++){
                for(int j=0; j<mBmpHeight; j++){
                    if( (curve[i*mBmpWidth+j] == CURVE_COLOR) && (lines[i*mBmpWidth+j] == LINES_COLOR)
                            && (minDist(j,i) > mBmpWidth*mBmpWidth/11000)
                            && (j<mBmpWidth) && (i<mBmpHeight) && (j>0) && (i>0)){
                        mDotsList.add(new Point(j,i));
                    }
                }
            }
            drawDots();
        }
    }
    private void setRef(){
        mRef = Bitmap.createBitmap(mBmpWidth, mBmpHeight, Bitmap.Config.ARGB_8888);
        mRefCanvas = new Canvas(mRef);
        Paint paint = new Paint();
        paint.setColor(REF_COLOR);
        if(mRefBegin != null){
            mRefCanvas.drawCircle(mRefBegin.mx, mRefBegin.my, 3, paint);
        }
        if(mRefEnd != null){
            mRefCanvas.drawCircle(mRefEnd.mx, mRefEnd.my, 3, paint);
        }
        if(mRefBegin != null && mRefEnd != null){
            paint.setColor(REF_LINE_COLOR);
            mRefCanvas.drawLine(mRefBegin.mx, mRefBegin.my, mRefEnd.mx, mRefEnd.my, paint);
            mRefDist = mRefBegin.distTo(mRefEnd);
            setLength();
        }
    }
    private void setRefBegin(int x, int y){
        mRefBegin = new Point(x, y);
        setRef();
    }
    private void setRefEnd(int x, int y){
        mRefEnd = new Point(x, y);
        setRef();
    }
    public void setLength() {
        double l = (double) Math.round(calcLenght()*1000)/1000;
        if(l==0){
            mLengthView.setText("0");
        }else{
            if(mSwitchRef.isChecked()) {
                mLengthView.setText(String.format("%s %s", l, mEditRefUnit.getText()));
            } else {
                mLengthView.setText(String.format("%d px", Math.round(calcLenght())));
            }
        }

    }
    private double calcLenght(){
        if(mCurvePointSelected){
            int nb_dots = mDotsList.size();
            double coef_unit_lines;
            if(mSwitchRef.isChecked() && mRefDist!=0 && mRefEnd!=null){
                double ref_lenght;
                double coef_ref;
                try{
                    ref_lenght = Long.valueOf(mEditRefLenght.getText().toString());
                    coef_ref = ref_lenght/mRefDist;
                }catch(Exception e){
                    coef_ref=1;
                }
                coef_unit_lines = coef_ref*mBmpWidth/mLinesNumber;
            } else {
                coef_unit_lines = mBmpWidth/mLinesNumber;
            }
            return (coef_unit_lines * 0.5 * nb_dots * PI) / mLinesAngle;
        }
        else{
            return 0;
        }
    }

    private void reDrawAll(){
        setCurve();
        setLines();
        calcDots();
        setLength();
        drawAll();
    }
    private void drawAll(){
        Bitmap currentBmp;
        //draw image/nothing
        if(mSwitchImage.isChecked() && mImageSelected){
            currentBmp = Bitmap.createBitmap(mPicture);
        } else {
            currentBmp = Bitmap.createBitmap(mBmpWidth, mBmpHeight, Bitmap.Config.ARGB_8888);
        }
        //canvas
        Canvas canvas = new Canvas(currentBmp);
        //draw curve if wanted
        if(mSwitchCurve.isChecked() && mCurvePointSelected) {
            canvas.drawBitmap(mCurve, 0f, 0f, null);
        }
        //draw lines
        if(mSwitchLines.isChecked() && mLines!=null){
            canvas.drawBitmap(mLines, 0f, 0f, null);
        }
        //draw dots
        if(mSwitchDots.isChecked() && mDots!=null){
            canvas.drawBitmap(mDots, 0f, 0f, null);
        }
        //draw reference
        if(mSwitchRef.isChecked() && mRef!=null){
            canvas.drawBitmap(mRef, 0,0, null);
        }
        mImageView.setImageBitmap(currentBmp);
        setLength();
    }










    private void setNoneState(){
        unSelectButtons();
        mRadioNone.setChecked(true);
        STATE = NONE;
    }
    @SuppressLint("ResourceAsColor")
    private void unSelectButtons(){
        mCurvePointBt.setTextColor(R.color.colorButton);
        mRefBeginBt.setTextColor(R.color.colorButton);
        mRefEndBt.setTextColor(R.color.colorButton);
    }
    private void setImage(){
        if(mSwitchBackground.isChecked()){
            mImageView.setBackgroundColor(Color.BLACK);
        }else {
            mImageView.setBackgroundColor(Color.TRANSPARENT);
        }
        if(mSelectedImage!=null) {
            mImageView.setImageURI(mSelectedImage);
        }else {
            mImageView.setImageBitmap(Bitmap.createBitmap(mBmpWidth, mBmpHeight, Bitmap.Config.ARGB_8888));
            mImageView.buildDrawingCache();
        }
        mImageView.buildDrawingCache();
        mPicture = mImageView.getDrawingCache();
        mImageSelected = true;
        float aspectRatio;
        try{
            aspectRatio = mPicture.getWidth() / (float) mPicture.getHeight();
        }catch(Exception e){
            aspectRatio = 3/4;
        }
        mBmpHeight = Math.round(mBmpWidth / aspectRatio);
        mPicture = Bitmap.createScaledBitmap(mPicture, mBmpWidth, mBmpHeight, false);
        mImageView.setImageBitmap(mPicture);

        mCurve = Bitmap.createBitmap(mBmpWidth, mBmpHeight, Bitmap.Config.ARGB_8888);
        setLines();
        supprDots();
        setRef();

        mSwitchLines.setChecked(false);
        mSwitchCurve.setChecked(false);
        mSwitchImage.setChecked(true);

        mImageView.setBackgroundColor(Color.TRANSPARENT);
        drawAll();

    }

/***************************************************************************************************
 * *************************************************************************************************
 * *************************************************************************************************
 * *************************************************************************************************
 * *************************************************************************************************
 * *************************************************************************************************
 * ************************************************************************************************/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            mSelectedImage = data.getData();
        }
        setImage();
    }

    /***************************************************************************************************
     * *************************************************************************************************
     * *************************************************************************************************
     * *************************************************************************************************
     * *************************************************************************************************
     * *************************************************************************************************
     * ************************************************************************************************/

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageSelected = false;
        mCurvePointSelected = false;
        mBmpWidth = 480;
        mBmpHeight = 533;
        mLinesNumber = 3;
        mLinesAngle = 3;
        mRefDist = 1;
        mDotsList = new ArrayList<>();
        mSelectedImage = null;


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setImage();
            }
        }, 250);



        final int btColor = getResources().getColor(R.color.colorAccent);


        mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.math_paper:
                        //https://github.com/pauldubois98/CauchyCroftonProject/tree/master/AndroidApp/media
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pauldubois98/CauchyCroftonApp/raw/master/project/paper/Cauchy-Crofton_Formula.pdf")));
                        return true;
                    case R.id.maths_expl:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://pauldubois98.github.io/CauchyCroftonApp/website/quick_expl.html")));
                        return true;
                    case R.id.app_expl:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pauldubois98/CauchyCroftonApp/raw/master/media/expl.pdf")));
                        return true;
                    case R.id.about_me:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://pauldubois98.github.io/CauchyCroftonApp/website/about.html")));
                        return true;
                    default:
                        return true;

                }
            }
        });




        mPickImgBt = findViewById(R.id.pick_img_bt);
        mPickImgBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImageSelected = false;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
        mSwitchImage = findViewById(R.id.switch_image);
        mSwitchImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                drawAll();
            }
        });
        mSwitchCurve = findViewById(R.id.switch_curve);
        mSwitchCurve.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                drawAll();
            }
        });
        mSwitchBackground = findViewById(R.id.switch_background);
        mSwitchBackground.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                setImage();
            }
        });
        mSeekWidth = findViewById(R.id.seek_width);
        mSeekWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mBmpWidth = i+MIN_WIDTH;
                mViewWidth.setText(String.valueOf(i+MIN_WIDTH));
                setImage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mViewWidth = findViewById(R.id.view_width);
        mDefaultWidth = findViewById(R.id.default_width_bt);
        mDefaultWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSeekWidth.setProgress(DEFAULT_WIDTH-5);
                mViewWidth.setText(String.valueOf(DEFAULT_WIDTH));
                mBmpWidth = DEFAULT_WIDTH;
                setImage();
            }
        });


        mCurvePointBt = findViewById(R.id.select_curve_pt_bt);
        mCurvePointBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mImageSelected){
                    mCurvePointSelected = false;
                    mCurvePointBt.setTextColor(btColor);
                    STATE = SELECT_CURVE;
                }
            }
        });
        mSeekTolerance = findViewById(R.id.seekTolerance);
        mSeekTolerance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setCurve();
                drawAll();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mViewNumberLines = findViewById(R.id.view_lines_number);
        mSeekNumberLines = findViewById(R.id.seek_nb_lines);
        mSeekNumberLines.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mLinesNumber = i+1;
                mViewNumberLines.setText(String.valueOf(mLinesNumber));
                setLines();
                drawAll();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mViewAngleLines = findViewById(R.id.view_lines_angle);
        mSeekAngleLines = findViewById(R.id.seek_angle_lines);
        mSeekAngleLines.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mLinesAngle = i+1;
                mViewAngleLines.setText(String.valueOf(mLinesAngle));
                setLines();
                drawAll();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mSwitchLines = findViewById(R.id.switch_lines);
        mSwitchLines.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                drawAll();
            }
        });

        mRadioNone = findViewById(R.id.radio_none_bt);
        mRadioNone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    unSelectButtons();
                    STATE = NONE;
                }
            }
        });
        mRadioAddDots = findViewById(R.id.radio_add_bt);
        mRadioAddDots.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    unSelectButtons();
                    STATE = ADD_DOT;
                }
            }
        });
        mRadioSupprDots = findViewById(R.id.radio_suppr_bt);
        mRadioSupprDots.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    unSelectButtons();
                    STATE = SUPPR_DOT;
                }
            }
        });
        mAutoDots = findViewById(R.id.auto_dots_bt);
        mAutoDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calcDots();
                drawAll();
            }
        });
        mSupprAllDots = findViewById(R.id.suppr_all_dots);
        mSupprAllDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                supprDots();
                drawAll();
            }
        });

        mSwitchDots = findViewById(R.id.switch_dots);
        mSwitchDots.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                drawAll();
            }
        });

        mSwitchRef = findViewById(R.id.switch_ref);
        mSwitchRef.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                drawAll();
            }
        });
        mRefBeginBt = findViewById(R.id.length_ref_begin_bt);
        mRefBeginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNoneState();
                mRefBeginBt.setTextColor(btColor);
                STATE = SELECT_REF1;
            }
        });
        mRefEndBt = findViewById(R.id.length_ref_end_bt);
        mRefEndBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNoneState();
                mRefEndBt.setTextColor(btColor);
                STATE = SELECT_REF2;
            }
        });
        mEditRefLenght = findViewById(R.id.editTextRefLenght);
        mEditRefLenght.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                setLength();
                return false;
            }
        });
        mEditRefUnit = findViewById(R.id.editTextRefUnit);
        mEditRefUnit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                setLength();
                return false;
            }
        });
        mLengthView = findViewById(R.id.length);
        mKeyboardDown = findViewById(R.id.keyboard_down_button);
        mKeyboardDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view2) {
                mEditRefLenght.clearFocus();
                mEditRefUnit.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                //Find the currently focused view, so we can grab the correct window token from it.
                View view = getCurrentFocus();
                //If no view currently has focus, create a new one, just so we can grab a window token from it
                if (view == null) {
                    view = mImageView;
                }
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                setLength();

            }
        });


        mImageView = findViewById(R.id.img_view);
        mImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (STATE){
                    case NONE:
                        //do nothing
                        break;
                    case SELECT_CURVE:
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                            int x = (int) motionEvent.getX()*mBmpWidth/mImageView.getWidth();
                            int y = (int) motionEvent.getY()*mBmpHeight/mImageView.getHeight();
                            setCurvePoint(x,y);
                            setCurve();
                            mSwitchCurve.setChecked(true);
                            drawAll();
                            //NONE STATE
                            setNoneState();
                        }
                        break;
                    case SELECT_REF1:
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                            int x = (int) motionEvent.getX()*mBmpWidth/mImageView.getWidth();
                            int y = (int) motionEvent.getY()*mBmpHeight/mImageView.getHeight();
                            setRefBegin(x,y);
                            mSwitchRef.setChecked(true);
                            drawAll();
                            //NONE STATE
                            setNoneState();
                        }
                        break;
                    case SELECT_REF2:
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                            int x = (int) motionEvent.getX()*mBmpWidth/mImageView.getWidth();
                            int y = (int) motionEvent.getY()*mBmpHeight/mImageView.getHeight();
                            setRefEnd(x,y);
                            mSwitchRef.setChecked(true);
                            drawAll();
                            //NONE STATE
                            setNoneState();
                        }
                        break;
                    case ADD_DOT:
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                            int x = (int) motionEvent.getX() * mBmpWidth / mImageView.getWidth();
                            int y = (int) motionEvent.getY() * mBmpHeight / mImageView.getHeight();
                            addDot(x, y);
                            drawAll();
                        }
                        break;
                    case SUPPR_DOT:
                        if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                            int x = (int) motionEvent.getX() * mBmpWidth / mImageView.getWidth();
                            int y = (int) motionEvent.getY() * mBmpHeight / mImageView.getHeight();
                            supprDot(x, y);
                            drawAll();
                        }
                        break;
                }
                return true;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}
