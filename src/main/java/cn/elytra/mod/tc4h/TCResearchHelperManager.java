package cn.elytra.mod.tc4h;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.wohaopa.tc4helper.TC4Helper;

public class TCResearchHelperManager {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    private static Future<?> lastFuture = null;

    public static void execute(IGuiResearchTableHelper helper) {
        if (!isIdle()) return;

        var data = helper.tc4h$getResearchNoteData();

        if (data != null && !data.isComplete()) {
            TC4Helper.LOG.info("Starting Research task: {}", data.key);
            lastFuture = EXECUTOR_SERVICE.submit(() -> {
                var aspectList = helper.tc4h$getAspectList();
                TCResearchResolver.INSTANCE.execute(data, aspectList, helper);
                TC4Helper.LOG.info("Finished Research task: {}", data.key);
            });
        }
    }

    public static boolean isIdle() {
        return lastFuture == null || lastFuture.isDone();
    }

    public static void forceStop() {
        if (lastFuture != null) lastFuture.cancel(true);
    }

    private static final Thread SAFE_GUARD_THREAD = new Thread(() -> {
        long startTime = 0;
        while(true) {
            if(startTime <= 0) {
                if(lastFuture != null && !lastFuture.isDone()) {
                    startTime = System.currentTimeMillis();
                }
            } else {
                if(lastFuture != null && !lastFuture.isDone()) {
                    if(System.currentTimeMillis() - startTime > 60 * 1000) { // time elapsed over than 1 minute
                        forceStop();
                        TC4Helper.LOG.warn("Safe Guard activated, {} ms elapsed", System.currentTimeMillis() - startTime);
                    }
                } else {
                    startTime = 0;
                }
            }
        }
    });

    static {
        SAFE_GUARD_THREAD.setName("TCResearchSafeGuard");
        SAFE_GUARD_THREAD.setDaemon(true);
        SAFE_GUARD_THREAD.start();
    }

}
