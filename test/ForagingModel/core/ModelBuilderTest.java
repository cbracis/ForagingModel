package ForagingModel.core;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ModelBuilderTest 
{
	@Test
	public void testModelBuilder()
	{
		ModelBuilder builder = new ModelBuilder();
		Model model = builder.build();
		
		Assert.assertTrue(model instanceof DefaultModel, "Default parameters should be valid");
	}
}
