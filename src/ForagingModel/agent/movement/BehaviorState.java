package ForagingModel.agent.movement;

public enum BehaviorState 
{
	Searching(1), 
	Feeding(2), 
	SingleState(3), 
	NotInitialized(4),
	Escape(5);
	
	private final int value;   

	BehaviorState(int value) 
    {
        this.value = value;
    }

    public int value() 
    { 
        return value; 
    }

}