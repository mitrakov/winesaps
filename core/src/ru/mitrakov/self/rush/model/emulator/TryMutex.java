package ru.mitrakov.self.rush.model.emulator;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mitrakov on 11.03.2018
 */
class TryMutex {
    private final ReentrantLock lock = new ReentrantLock();
    private boolean locked;
    private long cnt;

    void onlyOne(Runnable func) {
        if (!this.locked) {          // 1. if locked => return
            long cnt = this.cnt;
            lock.lock();             // 2. Lock()
            if (cnt == this.cnt) {
                this.locked = true;  // 3. locked = true
                this.cnt++;
                func.run();          // 4. run f()
                this.locked = false; // 5. locked = false
            }
            lock.unlock();           // 6. Unlock()
        }
    }
}
