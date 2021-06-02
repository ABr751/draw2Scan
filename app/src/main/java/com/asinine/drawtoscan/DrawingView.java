package com.asinine.drawtoscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import androidx.annotation.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class DrawingView extends View {

    private int pathIndex = 0;
    private ArrayList<Path> pathLists = new ArrayList<>();
    private ArrayList<Paint> paintLists = new ArrayList<>();
    private float startX = 0F;
    private float startY = 0F;

    private void init() {
        pathLists.add(new Path());
        paintLists.add(createPaint());
        pathIndex++;
    }

    public Paint createPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3F);
        return paint;
    }

    public void updatePaint(Path path) {
        if (ptListener != null) {
            ptListener.pathTouched(path);
        }
    }

    public Path createPath(MotionEvent event) {
        Path path = new Path();
        startX = event.getX();
        startY = event.getY();
        path.moveTo(startX, startY);
        return path;
    }

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DrawingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        for (int index = 0; index < pathIndex; index++) {
            Path path = pathLists.get(index);
            Paint paint = paintLists.get(index);
            paint.setStrokeWidth(10);
            canvas.drawPath(path, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                updateIndex(createPath(event));
                for (Path p : pathLists) {
                    RectF pBounds = new RectF();
                    p.computeBounds(pBounds, true);
                    if (pBounds.contains(startX, startY)) {
                        //select path
                        Path selected = p;// where selectedPath is assumed declared.
                        Log.d("selected", selected.toString());
                        updateIndex(p);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();
                Path path = pathLists.get(pathIndex - 1);
                ;
                path.lineTo(x, y);
                break;
            }
            default:
                break;
        }
        // Invalidate the whole view. If the view is visible.
        invalidate();
        return true;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    public void updateIndex(Path path) {
        if (pathLists.contains(path)) {
            updatePaint(path);
        } else {
            pathIndex = pathLists.size();
            pathLists.add(path);
            paintLists.add(createPaint());
            pathIndex++;
        }
    }

    public interface PathTouchListener {
          void pathTouched(Path path);
    }

    private PathTouchListener ptListener;

    public void setPathTouchListener(PathTouchListener listener) {
        this.ptListener = listener;
    }
}