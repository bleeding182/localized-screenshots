/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 David Medenjak
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package at.bleeding182.testing.instrumentationtest;

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.Until;
import android.view.View;
import android.view.WindowManager;

import com.robotium.solo.Condition;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import at.bleeding182.testing.instrumentationtest.modules.RandomModule;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertTrue;

/**
 * @author David Medenjak on 12.12.2015.
 */
@RunWith(Parameterized.class)
public class Screenshots {

    private static final long LAUNCH_TIMEOUT = 5 * 1000;
    private final Locale mLocale;
    private UiDevice mDevice;
    private Solo mSolo;

    @Parameterized.Parameters(name = "Locale: {0}")
    public static Collection<Locale> locales() {
        return Arrays.asList(new Locale("en"), new Locale("de"), new Locale("fr"));
    }

    public Screenshots(Locale locale) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        mLocale = locale;
        Configuration config = new Configuration();
        Locale.setDefault(mLocale);
        config.locale = mLocale;
        Resources resources = InstrumentationRegistry.getTargetContext().getApplicationContext().getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        Method method = getClass().getClassLoader()
                .loadClass("android.app.ApplicationPackageManager")
                .getDeclaredMethod("configurationChanged");
        method.setAccessible(true);
        method.invoke(null);


    }

    @Before
    public void setupTest() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        mDevice = UiDevice.getInstance(instrumentation);
        mSolo = new Solo(instrumentation);
    }

    public static class MockModule extends RandomModule {

        int provideRandomInt() {
            return 42;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void makeScreenshot() throws IOException {
        try {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // add test data
        App app = (App) InstrumentationRegistry.getTargetContext().getApplicationContext();

        try {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        UserComponent component = DaggerUserComponent.builder().randomModule(new MockModule()).build();
        app.setUserComponent(component);

        try {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        launchActivity();

        try {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // take screenshot
        final File file = new File("/sdcard/test/" + Locale.getDefault().getLanguage() + "/screenshot.png");
        assertEquals("Could not create directory", true, file.getParentFile().mkdirs() || file.getParentFile().exists());


        takeScreenshot("/sdcard/test/" + Locale.getDefault().getLanguage() + "/screenshot.png");

//        FileInputStream inputStream = new FileInputStream(file);
//        BitmapFactory.Options opt = new BitmapFactory.Options();
//        opt.inMutable = true;
//        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, opt);
//
//
//        inputStream.close();
//
//        int statusBarColor = bitmap.getPixel(mDevice.getDisplayWidth() - 2, 2);
//        int resourceId = app.getResources().getIdentifier("status_bar_height", "dimen", "android");
//        int statusBarHeight = app.getResources().getDimensionPixelSize(resourceId);
//
//        Paint paint = new Paint();
//        paint.setColor(statusBarColor);
//
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawRect(0, 0, mDevice.getDisplayWidth(), statusBarHeight, paint);
//
//        paint.setAntiAlias(true);
//        paint.setTypeface(Typeface.create("sans-serif-regular", 0));
//        paint.setTextAlign(Paint.Align.RIGHT);
//
//        paint.setTextSize(14 * app.getResources().getDisplayMetrics().scaledDensity);
//        paint.setColor(Color.argb((int) (255), 255, 255, 255));
//
//        Rect rect = new Rect();
//        paint.getTextBounds("10:00", 0, 5, rect);
//
//        final float dp8 = app.getResources().getDisplayMetrics().density * 8;
//
//        canvas.drawText("10:00",
//                mDevice.getDisplayWidth() - dp8,
//                (statusBarHeight / 2) - rect.exactCenterY(), paint);
//
//        int batteryId = app.getResources().getIdentifier("stat_sys_battery_100", "drawable", "android");
//        Bitmap battery = BitmapFactory.decodeResource(app.getResources(), batteryId);
//        paint.setColorFilter(new PorterDuffColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_IN));
//        canvas.drawBitmap(battery, mDevice.getDisplayWidth() - 2 * dp8 - rect.width() - battery.getWidth(),
//                (statusBarHeight - battery.getHeight()) / 2, paint);
//
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
    }

    public void takeScreenshot(final String filename) {
        View view = mSolo.getCurrentViews().get(0).getRootView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        File directory = new File(filename);
//        directory.mkdirs();

        if (bitmap != null) {
            try {
//                File outputFile = new File(directory, filename);
                FileOutputStream ostream = new FileOutputStream(directory);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                ostream.close();
            } catch (Exception e) {
                fail(e.getMessage());
            }
        }
    }

    private void launchActivity() {
        // Start from the home screen
        mDevice.pressHome();

        // Wait for launcher
        final String launcherPackage = mDevice.getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        mDevice.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);

        // Launch the app
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);

        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // Wait for the app to appear
        mDevice.wait(Until.hasObject(By.pkg(BuildConfig.APPLICATION_ID).depth(0)), LAUNCH_TIMEOUT);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        try {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

