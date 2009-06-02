/***
 * 
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * copyright holders nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.integracao;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;

public class CommandToExecuteTest extends AtDirectoryTest {

	@Test
	public void stopShouldStopProcessAndNotProceedWithIt() throws IOException, InterruptedException {
		final AtomicBoolean fail = new AtomicBoolean(false);
		givenA(new File(baseDir, "myScript.sh"), "sleep 15\ntouch customFile");
		final CommandToExecute cmd = new CommandToExecute("sh", "myScript.sh").at(baseDir);
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					if (cmd.run() == 0) {
						fail.set(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
					fail.set(true);
				}
			}
		});
		t.start();
		giveItSomeTimeToRun();
		cmd.stop();
		giveItAsMuchTimeAsItWants(t);
		Assert.assertFalse(new File(baseDir, "customFile").exists());
		Assert.assertFalse(fail.get());
	}

	private void giveItSomeTimeToRun() throws InterruptedException {
		Thread.sleep(1000);
	}

	@Test
	public void stopShouldStopProcessAndNotProceedWithSpawnedProcesses() throws IOException, InterruptedException {
		final AtomicBoolean fail = new AtomicBoolean(false);
		givenA(new File(baseDir, "sleepAndTouch.sh"), "sleep 15\ntouch customFile");
		givenA(new File(baseDir, "myScript.sh"), "sh sleepAndTouch &\n sleep 15");
		final CommandToExecute cmd = new CommandToExecute("sh", "myScript.sh").at(baseDir);
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					if (cmd.run() == 0) {
						fail.set(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
					fail.set(true);
				}
			}
		});
		t.start();
		giveItSomeTimeToRun();
		cmd.stop();
		giveItAsMuchTimeAsItWants(t);
		Assert.assertFalse(new File(baseDir, "customFile").exists());
		Assert.assertFalse(fail.get());
	}

	@Test
	public void stopTooLateShouldNotModifyAnything() throws IOException, InterruptedException {
		final AtomicBoolean success = new AtomicBoolean(false);
		givenA(new File(baseDir, "myScript.sh"), "touch customFile");
		final CommandToExecute cmd = new CommandToExecute("sh", "myScript.sh").at(baseDir);
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					if (cmd.run() == 0) {
						success.set(true);
					}
				} catch (Exception e) {
					e.printStackTrace();
					success.set(false);
				}
			}
		});
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
		giveItAsMuchTimeAsItWants(t);
		cmd.stop();
		Assert.assertTrue(new File(baseDir, "customFile").exists());
		Assert.assertTrue(success.get());
	}

	private void giveItAsMuchTimeAsItWants(Thread t) throws InterruptedException {
		if (t.isAlive()) {
			t.join();
		}
	}

}
