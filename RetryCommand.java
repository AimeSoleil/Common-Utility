import java.util.function.Supplier;

public class RetryCommand<T> {
    public static final int DEFAULT_MAX_RETRY_COUNT = 5;
    public static final int DEFAULT_TIME_TO_WAIT = 1000;

    private int retryCounter;
    private int maxRetries;
    private long timeToWait;
    private Supplier<T> task;

    public RetryCommand(Supplier<T> task) {
        this.maxRetries = DEFAULT_MAX_RETRY_COUNT;
        this.timeToWait = DEFAULT_TIME_TO_WAIT;
        this.task = task;
    }

    public RetryCommand(int maxRetries, long timeToWait, Supplier<T> task) {
        this.maxRetries = maxRetries;
        this.timeToWait = timeToWait;
        this.task = task;
    }

    public T runOnException(Class<? extends Throwable> targetEx) throws InterruptedException {
        try {
            return task.get();
        } catch (Throwable e) {
            if (targetEx.isInstance(e)) {
                return retry(targetEx);
            }

            throw  e;
        }
    }

    public int getRetryCounter() {
        return this.retryCounter;
    }

    private T retry(Class targetEx) throws InterruptedException {
        retryCounter = 0;
        while (retryCounter <= maxRetries) {
            try {
                return task.get();
            } catch (Throwable e) {
                retryCounter++;
                if (targetEx.isInstance(e)) {
                    if (getRetryCounter() > maxRetries) {
                        throw e;
                    }

                    Thread.sleep(timeToWait * getRetryCounter());
                } else {
                    throw e;
                }
            }
        }

        return null;
    }
}
