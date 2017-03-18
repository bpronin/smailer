package com.bopr.android.smailer;

import android.content.Context;
import android.content.pm.PackageManager;

import org.junit.Test;

import static android.Manifest.permission.BROADCAST_SMS;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link PermissionsChecker} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class PermissionsCheckerTest extends BaseTest {

    /**
     * Tests {@link PermissionsChecker#isPermissionsDenied(Context, String...)} method.
     *
     * @throws Exception when failed
     */
    @Test
    public void testIsPermissionsDenied() throws Exception {
        Context context = mock(Context.class);

        when(context.checkPermission(eq(BROADCAST_SMS), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_DENIED);
        assertTrue(PermissionsChecker.isPermissionsDenied(context, BROADCAST_SMS));

        when(context.checkPermission(eq(BROADCAST_SMS), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
        assertFalse(PermissionsChecker.isPermissionsDenied(context, BROADCAST_SMS));
    }
}