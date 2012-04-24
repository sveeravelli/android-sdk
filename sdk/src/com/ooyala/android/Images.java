package com.ooyala.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;

class Images {
  public static final int PLAY = 0;
  public static final int PAUSE = 1;
  public static final int FULLSCREEN = 2;
  public static final int SMALLSCREEN = 3;
  public static final int NEXT = 4;
  public static final int PREVIOUS = 5;

  public static GradientDrawable gradientBackground(GradientDrawable.Orientation orientation) {
    GradientDrawable gradient = new GradientDrawable(orientation, new int[] { 0xFF000000, 0x80151515 });
    gradient.setCornerRadius(0f);
    return gradient;
  }

  public static void play(Canvas c, Paint p, int width, int height, int marginPixels) {
    Path path = new Path();
    path.moveTo((float) marginPixels, (float) marginPixels);
    path.lineTo((float) width - (float) marginPixels, (float) marginPixels
        + ((float) height - 2f * (float) marginPixels) / 2f);
    path.lineTo((float) marginPixels, (float) height - (float) marginPixels);
    path.lineTo((float) marginPixels, (float) marginPixels);
    c.drawPath(path, p);
  }

  public static void pause(Canvas c, Paint p, int width, int height, int marginPixels) {
    Path path = new Path();
    float widthMinusMargins = width - 2f * marginPixels;
    float heightMinusMargins = height - 2f * marginPixels;
    // Left rectangle
    path.moveTo(marginPixels + 1.5f * widthMinusMargins / 8f, (float) marginPixels);
    path.lineTo(marginPixels + 3.5f * widthMinusMargins / 8f, (float) marginPixels);
    path.lineTo(marginPixels + 3.5f * widthMinusMargins / 8f, (float) marginPixels + heightMinusMargins);
    path.lineTo(marginPixels + 1.5f * widthMinusMargins / 8f, (float) marginPixels + heightMinusMargins);
    path.lineTo(marginPixels + 1.5f * widthMinusMargins / 8f, (float) marginPixels);
    // Right rectangle
    path.moveTo(marginPixels + 4.5f * widthMinusMargins / 8f, (float) marginPixels);
    path.lineTo(marginPixels + 6.5f * widthMinusMargins / 8f, (float) marginPixels);
    path.lineTo(marginPixels + 6.5f * widthMinusMargins / 8f, (float) marginPixels + heightMinusMargins);
    path.lineTo(marginPixels + 4.5f * widthMinusMargins / 8f, (float) marginPixels + heightMinusMargins);
    path.lineTo(marginPixels + 4.5f * widthMinusMargins / 8f, (float) marginPixels);
    c.drawPath(path, p);
  }

  public static void fullscreen(Canvas c, Paint p, int width, int height, int marginPixels) {
    Path path = new Path();
    float marginX = (float) marginPixels;
    float marginY = (float) marginPixels;
    // square-ify margins
    if (width > height) {
      marginX += ((float) (width - height)) / 2.0f;
    } else if (height > width) {
      marginY += ((float) (height - width)) / 2.0f;
    }
    float widthMinusMargins = width - 2.0f * marginX;
    float heightMinusMargins = height - 2.0f * marginY;
    // Top triangle
    path.moveTo(marginX, marginY);
    path.lineTo(marginX + (widthMinusMargins / 2.0f), marginY);
    path.lineTo(marginX, marginY + (heightMinusMargins / 2.0f));
    path.lineTo(marginX, marginY);
    // Bottom triangle
    path.moveTo(marginX + widthMinusMargins, marginY + (heightMinusMargins / 2.0f));
    path.lineTo(marginX + widthMinusMargins, marginY + heightMinusMargins);
    path.lineTo(marginX + (widthMinusMargins / 2.0f), marginY + heightMinusMargins);
    path.lineTo(marginX + widthMinusMargins, marginY + (heightMinusMargins / 2.0f));
    c.drawPath(path, p);
    // Stems
    Paint linePaint = new Paint();
    linePaint.setDither(true);
    linePaint.setColor(p.getColor());
    linePaint.setStyle(Paint.Style.STROKE);
    float strokeWidth = widthMinusMargins / 5.0f;
    linePaint.setStrokeWidth(strokeWidth);
    c.drawLine(marginX + strokeWidth, marginY + strokeWidth, marginX + widthMinusMargins / 2.0f - strokeWidth
        / 2.0f, marginY + heightMinusMargins / 2.0f - strokeWidth / 2.0f, linePaint);
    c.drawLine(marginX + widthMinusMargins - strokeWidth, marginY + heightMinusMargins - strokeWidth, marginX
        + widthMinusMargins / 2.0f + strokeWidth / 2.0f, marginY + heightMinusMargins / 2.0f + strokeWidth
        / 2.0f, linePaint);
  }

  public static void smallscreen(Canvas c, Paint p, int width, int height, int marginPixels) {
    Path path = new Path();
    float marginX = (float) marginPixels;
    float marginY = (float) marginPixels;
    // square-ify margins
    if (width > height) {
      marginX += ((float) (width - height)) / 2.0f;
    } else if (height > width) {
      marginY += ((float) (height - width)) / 2.0f;
    }
    float widthMinusMargins = width - 2.0f * marginX;
    float heightMinusMargins = height - 2.0f * marginY;
    // Stems
    Paint linePaint = new Paint();
    linePaint.setDither(true);
    linePaint.setColor(p.getColor());
    linePaint.setStyle(Paint.Style.STROKE);
    float strokeWidth = widthMinusMargins / 5.0f;
    linePaint.setStrokeWidth(strokeWidth);
    c.drawLine(marginX, marginY, marginX + widthMinusMargins / 2.0f - strokeWidth, marginY
        + heightMinusMargins / 2.0f - strokeWidth, linePaint);
    c.drawLine(marginX + widthMinusMargins, marginY + heightMinusMargins, marginX + widthMinusMargins / 2.0f
        + strokeWidth, marginY + heightMinusMargins / 2.0f + strokeWidth, linePaint);
    // Top triangle
    path.moveTo(marginX + (widthMinusMargins / 2.0f) - strokeWidth / 4.0f, marginY);
    path.lineTo(marginX + (widthMinusMargins / 2.0f) - strokeWidth / 4.0f, marginY
        + (heightMinusMargins / 2.0f) - strokeWidth / 4.0f);
    path.lineTo(marginX, marginY + (heightMinusMargins / 2.0f) - strokeWidth / 4.0f);
    path.lineTo(marginX + (widthMinusMargins / 2.0f) - strokeWidth / 4.0f, marginY);
    // Bottom triangle
    path.moveTo(marginX + widthMinusMargins / 2.0f + strokeWidth / 4.0f, marginY
        + (heightMinusMargins / 2.0f) + strokeWidth / 4.0f);
    path.lineTo(marginX + widthMinusMargins, marginY + (heightMinusMargins / 2.0f) + strokeWidth / 4.0f);
    path.lineTo(marginX + widthMinusMargins / 2.0f + strokeWidth / 4.0f, marginY + heightMinusMargins);
    path.lineTo(marginX + widthMinusMargins / 2.0f + strokeWidth / 4.0f, marginY
        + (heightMinusMargins / 2.0f) + strokeWidth / 4.0f);
    c.drawPath(path, p);
  }

  public static void next(Canvas c, Paint p, int width, int height, int marginPixels) {
    Path path = new Path();
    float widthMinusMargins = width - 2f * marginPixels;
    float heightMinusMargins = height - 2f * marginPixels;
    float barWidth = (widthMinusMargins / 10f);
    // Left Arrow
    path.moveTo((float) marginPixels, (float) marginPixels);
    path.lineTo((float) marginPixels + ((widthMinusMargins - barWidth) / 2f), (float) marginPixels
        + (heightMinusMargins) / 2f);
    path.lineTo((float) marginPixels, (float) marginPixels + heightMinusMargins);
    path.lineTo((float) marginPixels, (float) marginPixels);
    // Right Arrow
    path.moveTo((float) marginPixels + ((widthMinusMargins - barWidth) / 2f), (float) marginPixels);
    path.lineTo((float) marginPixels + widthMinusMargins - barWidth, (float) marginPixels
        + (heightMinusMargins) / 2f);
    path.lineTo((float) marginPixels + ((widthMinusMargins - barWidth) / 2f), (float) marginPixels
        + heightMinusMargins);
    path.lineTo((float) marginPixels + ((widthMinusMargins - barWidth) / 2f), (float) marginPixels);
    c.drawPath(path, p);
    // Vertical Bar
    Paint linePaint = new Paint();
    linePaint.setDither(true);
    linePaint.setColor(p.getColor());
    linePaint.setStyle(Paint.Style.STROKE);
    linePaint.setStrokeWidth(barWidth);
    c.drawLine((float) marginPixels + (widthMinusMargins) - barWidth / 2f, (float) marginPixels,
        (float) marginPixels + (widthMinusMargins) - barWidth / 2f,
        (float) marginPixels + heightMinusMargins, linePaint);
  }

  public static void previous(Canvas c, Paint p, int width, int height, int marginPixels) {
    Path path = new Path();
    float widthMinusMargins = width - 2f * marginPixels;
    float heightMinusMargins = height - 2f * marginPixels;
    float barWidth = (widthMinusMargins / 10f);
    // Vertical Bar
    Paint linePaint = new Paint();
    linePaint.setDither(true);
    linePaint.setColor(p.getColor());
    linePaint.setStyle(Paint.Style.STROKE);
    linePaint.setStrokeWidth(barWidth);
    c.drawLine((float) marginPixels + barWidth / 2f, (float) marginPixels, (float) marginPixels + barWidth
        / 2f, (float) marginPixels + heightMinusMargins, linePaint);
    // Left Arrow
    path.moveTo((float) marginPixels + barWidth, (float) marginPixels + heightMinusMargins / 2f);
    path.lineTo((float) marginPixels + barWidth + ((widthMinusMargins - barWidth) / 2f), (float) marginPixels);
    path.lineTo((float) marginPixels + barWidth + ((widthMinusMargins - barWidth) / 2f), (float) marginPixels
        + heightMinusMargins);
    path.lineTo((float) marginPixels + barWidth, (float) marginPixels + heightMinusMargins / 2f);
    // Right Arrow
    path.moveTo((float) marginPixels + barWidth + ((widthMinusMargins - barWidth) / 2f), (float) marginPixels
        + heightMinusMargins / 2f);
    path.lineTo((float) marginPixels + widthMinusMargins, (float) marginPixels);
    path.lineTo((float) marginPixels + widthMinusMargins, (float) marginPixels + heightMinusMargins);
    path.lineTo((float) marginPixels + barWidth + ((widthMinusMargins - barWidth) / 2f), (float) marginPixels
        + heightMinusMargins / 2f);
    c.drawPath(path, p);
  }

  public static void drawImage(int i, Context ct, Canvas c, int background, int fill, int width, int height,
      int marginDP, boolean glow) {
    c.drawColor(background);
    Paint p = new Paint();
    p.setDither(true);
    p.setAntiAlias(true);
    p.setColor(fill);
    p.setStyle(Paint.Style.FILL);
    int marginPixels = dpToPixels(ct, marginDP);
    switch (i) {
      case PLAY:
        double playIconWidth = Math.sqrt(3) / 2 * height;
        Images.play(c, p, (int) playIconWidth, height, marginPixels);
        break;
      case PAUSE:
        Images.pause(c, p, width, height, marginPixels);
        break;
      case FULLSCREEN:
        Images.fullscreen(c, p, width, height, marginPixels);
        break;
      case SMALLSCREEN:
        Images.smallscreen(c, p, width, height, marginPixels);
        break;
      case NEXT:
        Images.next(c, p, width, height, marginPixels);
        break;
      case PREVIOUS:
        Images.previous(c, p, width, height, marginPixels);
        break;
    }
    if (glow) {
      GradientDrawable glowDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
          new int[] { 0xFFFFFFFF, 0x00FFFFFF });
      glowDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
      glowDrawable.setGradientRadius(width / 2);
      glowDrawable.setBounds(0, 0, width, height);
      glowDrawable.setDither(true);
      glowDrawable.draw(c);
    }
  }

  public static int dpToPixels(Context c, int dp) {
    final float scale = c.getResources().getDisplayMetrics().density;
    return (int) (dp * scale + 0.5f);
  }
}
