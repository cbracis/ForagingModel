package ForagingModel.schedule;

public enum SchedulePriority 
{
	First(Integer.MIN_VALUE),
	ForagerMove(1),
	ForagerConsume(2),
	ForagerDepositScent(3),
	ResourceGrow(4),
	MemoryDecay(5),
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
    
    public static SchedulePriority fromValue(int value)
    {
        for (SchedulePriority sp : values()) 
        {
            if (sp.value == value)
            {
                return sp;
            }
        }
        return null;

    }
}
