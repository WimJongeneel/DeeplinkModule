// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package deeplink.actions;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IDataType;
import com.mendix.systemwideinterfaces.core.IDataType.DataTypeEnum;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive;
import com.mendix.webui.CustomJavaAction;
import com.mendix.webui.FeedbackHelper;
import deeplink.proxies.Attribute;
import deeplink.proxies.Entity;
import deeplink.proxies.Microflow;

/**
 * Regenerates the microflow and entity reflection structure used by the deeplink configuration screens.
 * 
 * Returns true (always)
 */
public class ReadMicroflows extends CustomJavaAction<java.lang.Boolean>
{
	public ReadMicroflows(IContext context)
	{
		super(context);
	}

	@java.lang.Override
	public java.lang.Boolean executeAction() throws Exception
	{
		// BEGIN USER CODE
		removeAll(Entity.getType());
		removeAll(Attribute.getType());
		removeAll(Microflow.getType());
		
		IContext c = getContext();
		
		for(IMetaObject meta : Core.getMetaObjects()) 
		{
			Entity entity = Entity.initialize(c, Core.instantiate(c, Entity.entityName));
			entity.setName(meta.getName());
			Core.commit(c, entity.getMendixObject());
			
			for (IMetaPrimitive prim : meta.getMetaPrimitives())
				if (!prim.isVirtual())
				{
					Attribute attr = Attribute.initialize(c, Core.instantiate(c, Attribute.entityName));
					attr.setName(prim.getName());
					attr.setAttribute_Entity(entity);
					Core.commit(c, attr.getMendixObject());
				}
		}
		
		for(String mf : Core.getMicroflowNames())
		{
			Map<String, IDataType> args = Core.getInputParameters(mf);

			IMendixObject datatype = null;
			boolean hasarg = false;
			boolean stringarg = false;
			boolean objectarg = false;
			boolean applicableForDeeplink = true;
			StringBuilder argumentExample = new StringBuilder();
			
			for (Entry<String, IDataType> entry : args.entrySet()) {
				hasarg = true;
				IDataType type = entry.getValue();
                
				//Special case, string type argument
				if (type.getType() == DataTypeEnum.String) {
					argumentExample.append(entry.getKey()).append("=YourValue&");
					stringarg = true;
				} else if( type.getType() == DataTypeEnum.Object ) {
					IMendixObject mo = StartDeeplinkJava.query(
					        c, Entity.getType(), Entity.MemberNames.Name, type.getObjectType());
                    argumentExample = new StringBuilder("YourValue&");
                    if (mo != null) {
                        datatype = mo;
                        stringarg = false;
                        objectarg = true;
                    }
                    
                    //If we have an object param we only support one input value
                    break;
                }
                else {
                    applicableForDeeplink = false;
                    break;
                }
			}
			if (!applicableForDeeplink || datatype == null && hasarg && !stringarg) //datatype is not entity or string
				continue;
			
			Microflow flow = Microflow.initialize(c, Core.instantiate(c, Microflow.entityName));
			flow.setName(mf);
			flow.setModule(mf.substring(0,mf.indexOf('.')));
			
			if( !"".equals(argumentExample.toString()) )
				flow.setArgumentExample((stringarg?"?":"/") + argumentExample.substring(0,argumentExample.length()-1));
			if (datatype != null)
				flow.setparam(Entity.initialize(c, datatype));
			if (stringarg)
				flow.setUseStringArg(true);
			if(objectarg)
				flow.setUseObjectArgument(true);
			
			
			Core.commit(c, flow.getMendixObject());
		}

		FeedbackHelper.addRefreshClass(getContext(), Microflow.entityName);
		FeedbackHelper.addRefreshClass(getContext(), Attribute.entityName);
		FeedbackHelper.addRefreshClass(getContext(), Entity.entityName);
		
		return true;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "ReadMicroflows";
	}

	// BEGIN EXTRA CODE
	public void removeAll(String type) throws CoreException {
		List<IMendixObject> objs = Core.retrieveXPathQuery(getContext(), "//" + type);
		Core.delete(getContext(), objs);
	}
	// END EXTRA CODE
}
