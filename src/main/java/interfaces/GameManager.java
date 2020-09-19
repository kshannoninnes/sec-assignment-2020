package interfaces;

public interface GameManager extends BoardManager
{
    void start();
    void stop();
    void setThreadHandler(ThreadManager threadHandler);
}
