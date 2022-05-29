// This file is part of PGLComponent.
// Copyright (c) 2022 Alexander Schütz
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

package io.github.alexanderschuetz97.pglcomponent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executor singleton for async tasks.
 * Note: JOGL uses its own threading mechanism for FPS Animation.
 * This executor is only for internal PGLComponent tasks.
 */
public class PGLComponentExecutor {

    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static Executor INSTANCE;

    /**
     * Change the executor to your own executor.
     * The executor should expect the Runnable to run for a long time and not interrupt the threads.
     * The executor should also not queue a Runnable indefinitely.
     *
     * If the executor is not set before it is required for the first time then it is initialized with a default executor.
     * This method returns the old executor if it was set.
     */
    public static synchronized Executor setExecutor(Executor executor) {
        Executor old = INSTANCE;
        INSTANCE = executor;
        return old;
    }

    public static synchronized Executor getExecutor() {
        if (INSTANCE == null) {
            setExecutor(Executors.newCachedThreadPool(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread t = new Thread(runnable, "PGLComponentExecutor " + COUNTER.get());
                    try {
                        t.setDaemon(true);
                    } catch (Exception ex) {
                        //DC
                    }
                    return t;
                }
            }));
        }
        return INSTANCE;
    }

    public static void execute(Runnable runnable) {
        getExecutor().execute(runnable);
    }
}
