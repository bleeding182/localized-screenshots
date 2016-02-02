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
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.util.Log;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

/**
 * @author David Medenjak on 12.12.2015.
 */
@RunWith(Parameterized.class)
public class Localization {

    private static final long LAUNCH_TIMEOUT = 5 * 1000;
    private final Locale mLocale;
    private UiDevice mDevice;

    @Parameterized.Parameters(name = "Locale: {0}")
    public static Collection<Locale> locales() {
        return Arrays.asList(
                new Locale("de"),
                new Locale("en"),
                new Locale("fr")
        );
    }

    public void log(String text) {
        Log.d("LocalizationTest", text);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public Localization(Locale locale) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Context context = InstrumentationRegistry.getTargetContext();
        log(context.toString());
        log(context.getApplicationContext().toString());

        mLocale = locale;
        Configuration config = new Configuration();
        Locale.setDefault(mLocale);
        config.setLocale(mLocale);

        Object thread = getClass().getClassLoader()
                .loadClass("android.app.ActivityThread")
                .getMethod("currentActivityThread")
                .invoke(null);
        Method method = getClass().getClassLoader()
                .loadClass("android.app.ActivityThread")
                .getMethod("applyConfigurationToResources", Configuration.class);
        method.invoke(thread, config);
    }

    @Before
    public void setupTest() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        mDevice = UiDevice.getInstance(instrumentation);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void makeScreenshot() throws IOException {
        launchActivity();
        Context context = InstrumentationRegistry.getTargetContext();
        PackageManager pm = context.getPackageManager();
        CharSequence text = pm.getText(context.getPackageName(), R.string.app_name, context.getApplicationInfo());

        Assert.assertEquals(get(), text.toString());

        Assert.assertEquals("Target Context wrong locale", mLocale.toString(),
                context.getResources().getConfiguration().locale.toString());

        Assert.assertEquals("App Context wrong locale", mLocale.toString(),
                context.getApplicationContext().getResources().getConfiguration().locale.toString());

        // Check the toolbar for the right localization, will fail for every run after the first
        Assert.assertTrue(get() + " not found", mDevice.findObject(new UiSelector().text(get())).waitForExists(500));
    }

    public String get() {
        switch (mLocale.toString()) {
            case "en":
                return "English";
            case "de":
                return "Deutsch";
            case "fr":
                return "French";
        }
        return "";
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
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        try {
            context = InstrumentationRegistry.getInstrumentation().newApplication(App.class,
                    InstrumentationRegistry.getTargetContext());
            Assert.assertEquals(context.getResources().getConfiguration().locale.toString(), mLocale.toString());
            log("New: " + context.toString());

            Assert.assertEquals(mLocale.toString(), context.getPackageManager()
                    .getResourcesForActivity(intent.getComponent())
                    .getConfiguration().locale.toString());


            ActivityInfo activityInfo = context.getPackageManager()
                    .getActivityInfo(intent.getComponent(), PackageManager.GET_META_DATA);
            int labelRes = activityInfo
                    .labelRes;
            log(labelRes + context.getResources().getString(labelRes));
            log(activityInfo.name);
            log("non " + activityInfo.nonLocalizedLabel);

        } catch (Exception e) {
            fail(e.getMessage());
        }

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

