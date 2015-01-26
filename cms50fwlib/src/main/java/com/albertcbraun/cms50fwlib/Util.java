/*
 * Copyright (c) 2015 Albert C. Braun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.albertcbraun.cms50fwlib;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

/**
 * Methods which are used in more than one class and
 * do not seem to belong to any other specific class.
 * <p>
 * Created by albertb on 1/14/2015.
 */
class Util {

    static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.US);

    /**
     * Verifies that the ExecutorService in question is still active, and if so,
     * requests an immediate shutdown.
     *
     * @param executorService a service which should be shut down immediately.
     */
    static void safeShutdown(ExecutorService executorService) {
        if (executorService != null && !executorService.isTerminated() && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    /**
     * Convenience method for logging via {@link com.albertcbraun.cms50fwlib.CMS50FWConnectionListener#onLogEvent(long, String)}
     * with a timestamp.
     * @param listener the callback implemented by the client of this library
     * @param message any message which the client may wish to see logged
     */
    static void log(CMS50FWConnectionListener listener, String message) {
        listener.onLogEvent(System.currentTimeMillis(), message);
    }

    static String formatString(String format, Object... objects) {
        return String.format(Locale.US, format, objects);
    }

}
