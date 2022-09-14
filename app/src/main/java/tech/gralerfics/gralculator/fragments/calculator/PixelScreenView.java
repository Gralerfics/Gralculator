package tech.gralerfics.gralculator.fragments.calculator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import tech.gralerfics.gralculator.R;
import tech.gralerfics.gralculator.core.Calculator;

public class PixelScreenView extends View {

    /* ========================= View ========================= */
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int symbolWidth, symbolHeight;
    private int screenWidth, screenHeight;
    private int columnNum, rowNum;
    private int screenPaddingVertical, screenPaddingHorizontal;
    private int screenPaddingRaw;

    public PixelScreenView(Context context) {
        super(context);
    }

    public PixelScreenView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PixelScreenView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PixelScreenView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initSize() {
        screenPaddingRaw = 10;

        screenWidth = this.getWidth() - screenPaddingRaw * 2;
        columnNum = 21;
        symbolWidth = (int) Math.floor(1.0 * screenWidth / columnNum);
        screenPaddingHorizontal = (screenWidth - symbolWidth * columnNum) / 2;

        screenHeight = this.getHeight() - screenPaddingRaw * 2;
        symbolHeight = symbolWidth * 2;
        rowNum = (int) Math.floor(1.0 * screenHeight / symbolHeight);
        screenPaddingVertical = (screenHeight - symbolHeight * rowNum) / 2;
    }

    private void initPaint() {
        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(symbolHeight);
        mPaint.setTypeface(ResourcesCompat.getFont(getContext(), R.font.fz_pixel_14));
    }

    private int getX(int column) {
        return screenPaddingRaw + screenPaddingHorizontal + column * symbolWidth;
    }

    private int getY(int row) {
        return screenPaddingRaw + screenPaddingVertical + (int) Math.floor((row + 0.785714) * symbolHeight);
    }

    private void printString(Canvas canvas, String str, int column, int row) {
        canvas.drawText(str, getX(column), getY(row), mPaint);
    }

    private void printStringWithLeftAlignedAndAutoEnter(Canvas canvas, String str, int row) {
        int len = str.length();
        for (int r = 0; r <= len / columnNum; r ++) {
            printString(
                    canvas,
                    str.substring(r * columnNum, Math.min((r + 1) * columnNum, str.length())),
                    0,
                    row + r
            );
        }
    }

    private void printStringWithRightAligned(Canvas canvas, String str, int row) {
        printString(canvas, str, columnNum - str.length(), row);
    }

    private void printCursor(Canvas canvas) {
        int c = cursorPos % columnNum;
        int r = cursorPos / columnNum;
        canvas.drawText("|", getX(c) - symbolWidth / 2, getY(r), mPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        printStringWithLeftAlignedAndAutoEnter(canvas, currentInput.toString(), 0);
        printStringWithRightAligned(canvas, currentOutput.toString(), rowNum - 1);
        printCursor(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initSize();
        initPaint();
    }

    /* ========================= Content ========================= */
    public StringBuilder currentInput = new StringBuilder();
    private StringBuilder currentOutput = new StringBuilder();
    private int cursorPos;

    private void initCursor() {
        cursorPos = 0;
    }

    private void clearOutput() {
        currentOutput.delete(0, currentOutput.length());
    }

    public void eventInput(String str) {
        currentInput.insert(cursorPos, str);

        cursorPos += str.length();
        clearOutput();

        requestLayout();
    }

    public void eventLeft() {
        if (cursorPos > 0) cursorPos --;

        requestLayout();
    }

    public void eventRight() {
        if (cursorPos < currentInput.length()) cursorPos ++;

        requestLayout();
    }

    public void eventDelete() {
        if (cursorPos > 0) currentInput.deleteCharAt(cursorPos - 1);

        if (cursorPos > 0) cursorPos --;
        clearOutput();

        requestLayout();
    }

    public void eventClearall() {
        currentInput.delete(0, currentInput.length());

        initCursor();
        clearOutput();

        requestLayout();
    }

    public void eventExecute() {
        clearOutput();
        currentOutput.append(Calculator.evalResult(currentInput.toString()));

        requestLayout();
    }

}
