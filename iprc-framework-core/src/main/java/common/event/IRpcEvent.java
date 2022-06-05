package common.event;

public interface IRpcEvent {
    Object getData();

    IRpcEvent setData(Object data);
}
