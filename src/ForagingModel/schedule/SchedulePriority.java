package ForagingModel.schedule;

public enum SchedulePriority 
{
	First(Integer.MIN_VALUE),
	ForagerMove(1),
	ForagerConsume(2),
	ResourceGrow(3),
	MemoryDecay(4),
	Visualize(10),
	Last(Integer.MAX_VALUE);
	
	private final int value;   

	SchedulePriority(int value) 
    {
        this.value = value;
    }

    public int value() 
    { 
        return value; 
    }
}
