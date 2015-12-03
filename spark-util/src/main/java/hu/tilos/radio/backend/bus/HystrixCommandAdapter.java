package hu.tilos.radio.backend.bus;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import scala.util.Try;

public class HystrixCommandAdapter<T extends Command> extends HystrixCommand<Try<T>> {

    private Handler<T> handler;

    private T command;

    public HystrixCommandAdapter(String name, Handler<T> handler, T command) {
        super(HystrixCommandGroupKey.Factory.asKey(command.getClass().getName()));
        this.handler = handler;
        this.command = command;
    }

    @Override
    protected Try<T> run() throws Exception {
        return handler.handle(command);
    }
}
