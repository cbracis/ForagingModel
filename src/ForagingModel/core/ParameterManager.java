package ForagingModel.core;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import ForagingModel.core.Parameters.Parameter;

public class ParameterManager implements Iterable<Parameters>
{
	private static Logger logger = Logger.getLogger( ParameterManager.class.getName() );
	private PropertiesConfiguration config;
	private Iterator<Parameters> iterator;
	
	protected ParameterManager( String propertiesFile )
	{
		iterator = new DefaultParameterManagerIterator();
		Parameters parameters = Parameters.get();
		Map<Parameter,List<String>> multiValuedParameters = new HashMap<Parameter, List<String>>();
		ReferentialParameterMap referentialParameters = new ReferentialParameterMap();
		try
		{
			if ( propertiesFile != null && propertiesFile != "" )
			{
					// log4j
					//PropertyConfigurator.configure( propertiesFile );
					
					// model configuration
					config = new PropertiesConfiguration();
					config.setListDelimiter( ';' );
					config.setFileName( propertiesFile );
					config.load();
					
					for ( Parameter parameter : Parameter.values() )
					{
						String[] values = config.getStringArray( parameter.toString() );
						if ( values.length > 0 )
						{
							if (isParameterName(values[0]))
							{
								// must be a parameter that has already been specified
								Parameter masterParam = Parameter.valueOf(values[0]);
								parameters.set( parameter, parameters.get(masterParam));
								referentialParameters.put(masterParam, parameter);
							}
							else
							{
								parameters.set( parameter, values[0] );
								if ( values.length > 1 )
								{
									multiValuedParameters.put( parameter, Arrays.asList(values) );
								}
							}
						}
					}
					if ( !multiValuedParameters.isEmpty() )
					{
						iterator = new MultiParameterIterator( multiValuedParameters, referentialParameters );
					}
					parameters.init();
	
			}
			else
			{
				// log4j
				//BasicConfigurator.configure();
			}
		} 
		catch ( ConfigurationException e )
		{
			throw new ForagingModelException( "Problem reading configuration", e );
		}
		catch ( ParseException e )
		{
			throw new ForagingModelException( "Error parsing parameter", e );
		}

	}
	
	protected boolean isParameterName(String value)
	{
		boolean isName = false;
		try 
		{
            Parameter.valueOf(value);
            isName = true;
        } 
		catch (IllegalArgumentException ignored) { }
		
		return isName;
	}

	public Iterator<Parameters> iterator()
	{
		return iterator;
	}
	
	class ParameterManagerIterator implements Iterator<Parameters>
	{
		Parameter parameter;
		List<String> values;
		List<Parameter> slaves;
		int currentIndex;
		
		private ParameterManagerIterator( Parameter parameter, List<String> values, List<Parameter> slaves ) 
		{
			this.parameter = parameter;
			this.values = values;
			this.slaves = slaves;
			this.currentIndex = 0;
		}

		public boolean hasNext()
		{
			if ( currentIndex < values.size() )
				return true;
			else
				return false;
		}

		public Parameters next()
		{
			String value = values.get( currentIndex );
			//logger.debug( String.format( "Setting parameter %s to %s", parameter, value ) );
			try
			{
				Parameters params = Parameters.get();
			
				params.set( parameter, value );
				if ( slaves != null )
				{
					for ( Parameter slave : slaves )
					{
						params.set( slave, value );
					}
				}
				currentIndex++;
				return params;

			} catch ( Exception e )
			{
				throw new ForagingModelException( String.format( "Error parsing parameter %s with value %s", parameter, value ), e );
			}
		}

		public void remove()
		{
			throw new UnsupportedOperationException("Parameters are not removeable");
		}
		
	}
	
	class MultiParameterIterator implements Iterator<Parameters>
	{
		private Map<Parameter, List<String>> multiValuedParameters;
		private List<Parameter> keys;
		private List<Iterator<Parameters>> iterators;
		private ReferentialParameterMap referentialParameters;
		
		private MultiParameterIterator( Map<Parameter, List<String>> multiValuedParameters, ReferentialParameterMap referentialParameters )
		{
			this.multiValuedParameters = multiValuedParameters;
			this.referentialParameters = referentialParameters;
			
			keys = new ArrayList<Parameter>( multiValuedParameters.keySet() );
			Collections.sort( keys );  // To ensure a consistent order for tests
			iterators = new ArrayList<Iterator<Parameters>>();

			for( Parameter parameter : keys )
			{
				iterators.add( new ParameterManagerIterator( parameter, 
						multiValuedParameters.get( parameter ), 
						referentialParameters.get( parameter ) ) );
			}

			// initialize system by calling next() on all but the last
			for ( int i = 0; i < iterators.size() - 1; i++ )
			{
				iterators.get( i ).next();
			}
		}

		public boolean hasNext()
		{
			boolean hasNext = false;
			for ( Iterator<Parameters> iterator : iterators )
			{
				hasNext = hasNext || iterator.hasNext();
			}
			return hasNext;
		}

		public Parameters next()
		{
			Parameters next = null;
			for( int i = iterators.size() - 1; i >= 0; i -- )
			{
				if ( iterators.get( i ).hasNext() )
				{
					resetIterators( i + 1 );
					next = iterators.get( i ).next();
					break;
				}
			}
			return next;
		}

		public void remove()
		{
			throw new UnsupportedOperationException("Parameters are not removeable");
		}
		
		private void resetIterators( int index )
		{
			for ( int i = index; i < iterators.size(); i++ )
			{
				Parameter parameter = keys.get( i );
				List<String> values = multiValuedParameters.get( parameter );
				List<Parameter> slaves = referentialParameters.get( parameter );
				
				iterators.set( i, new ParameterManagerIterator( parameter, values, slaves ) );
				// Need to move next to account for current return, will also reset Parameters value correctly to first
				iterators.get( i ).next();
			}
		}
	}
	
	class DefaultParameterManagerIterator implements Iterator<Parameters>
	{
		boolean state = true;
		
		private DefaultParameterManagerIterator() {}

		public boolean hasNext()
		{
			return state;
		}

		public Parameters next()
		{
			state = false;
			return Parameters.get();
		}

		public void remove()
		{
			throw new UnsupportedOperationException("Parameters are not removeable");
		}
		
	}
	
	class ReferentialParameterMap
	{
		private Map<Parameter, List<Parameter>> map;
		
		public ReferentialParameterMap()
		{
			map = new HashMap<Parameter, List<Parameter>>();
		}
		
		public void put(Parameter master, Parameter slave)
		{
			if (map.containsKey(slave))
			{
				throw new ForagingModelException("Multiple levels of indirection not allowed. " + slave + " already exists as a master parameter.");
			}
			
			if (map.containsKey(master))
			{
				map.get(master).add(slave);
			}
			else
			{
				List<Parameter> slaves = new ArrayList<Parameter>();
				slaves.add(slave);
				map.put(master, slaves);
			}
		}
		
		public List<Parameter> get(Parameter master)
		{
			return map.get(master);
		}
	}
}
