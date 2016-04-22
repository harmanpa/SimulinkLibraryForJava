/*-------------------------------------------------------------------------+
|                                                                          |
| Copyright 2005-2011 The ConQAT Project                                   |
|                                                                          |
| Licensed under the Apache License, Version 2.0 (the "License");          |
| you may not use this file except in compliance with the License.         |
| You may obtain a copy of the License at                                  |
|                                                                          |
|    http://www.apache.org/licenses/LICENSE-2.0                            |
|                                                                          |
| Unless required by applicable law or agreed to in writing, software      |
| distributed under the License is distributed on an "AS IS" BASIS,        |
| WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. |
| See the License for the specific language governing permissions and      |
| limitations under the License.                                           |
+-------------------------------------------------------------------------*/
package org.conqat.lib.simulink.model;

/**
 * This class contains constants used by the Simulink model builder. These
 * constants are section and parameter names that refer to the MDL file. Section
 * and parameters are distinguished by the prefix of the constants (SECTION vs
 * PARAM). The remainder of the constant is just like the name in the MDL file.
 * We use mixed case here to express the case differences found in the MDL file,
 * e.g. 'Name' vs 'name'.
 * 
 * @author $Author: heinemann $
 * @version $Rev: 51948 $
 * @ConQAT.Rating GREEN Hash: 41244DF4A085DAEA06DC6B156ED2371B
 */
public class SimulinkConstants {

	/** Color code red. */
	public static final String COLOR_Red = "red";

	/** Color code yellow. */
	public static final String COLOR_Yellow = "yellow";

	/** Color code white. */
	public static final String COLOR_White = "white";

	/** Color code green. */
	public static final String COLOR_Green = "green";

	/** Color code cyan. */
	public static final String COLOR_Cyan = "cyan";

	/** Color code blue. */
	public static final String COLOR_Blue = "blue";

	/** Color code black. */
	public static final String COLOR_Black = "black";

	/** Color code orange. */
	public static final String COLOR_Orange = "orange";

	/** Color code light blue. */
	public static final String COLOR_LightBlue = "lightBlue";

	/** Model section. */
	public static final String SECTION_Model = "Model";

	/** Model information. */
	public static final String SECTION_ModelInformation = "ModelInformation";

	/** Stateflow section. */
	public static final String SECTION_Stateflow = "Stateflow";

	/** Children section. */
	public static final String SECTION_Children = "Children";

	/** Library section. */
	public static final String SECTION_Library = "Library";

	/** Destination section in Stateflow transitions. */
	public static final String SECTION_dst = "dst";

	/** Source section in Stateflow transitions. */
	public static final String SECTION_src = "src";

	/** Transition section (Stateflow) */
	public static final String SECTION_transition = "transition";

	/** Junction section (Stateflow) */
	public static final String SECTION_junction = "junction";

	/** Event section (Stateflow) */
	public static final String SECTION_event = "event";

	/** Data section (Stateflow) */
	public static final String SECTION_data = "data";

	/** Target section (Stateflow) */
	public static final String SECTION_target = "target";

	/** Instance section (Stateflow) */
	public static final String SECTION_instance = "instance";

	/** State section (Stateflow) */
	public static final String SECTION_state = "state";

	/** Chart section (Stateflow) */
	public static final String SECTION_chart = "chart";

	/** Machine section (Stateflow) */
	public static final String SECTION_machine = "machine";

	/** Block parameter defaults section. */
	public static final String SECTION_BlockParameterDefaults = "BlockParameterDefaults";

	/** Block defaults section. */
	public static final String SECTION_BlockDefaults = "BlockDefaults";

	/** System defaults section. */
	public static final String SECTION_SystemDefaults = "SystemDefaults";

	/** Annotation defaults section. */
	public static final String SECTION_AnnotationDefaults = "AnnotationDefaults";

	/** Line defaults section. */
	public static final String SECTION_LineDefaults = "LineDefaults";

	/** Block section. */
	public static final String SECTION_Block = "Block";

	/** Array section. */
	public static final String SECTION_Array = "Array";

	/** SimulationSettings section. */
	public static final String SECTION_SimulationSettings = "SimulationSettings";

	/** Parameter section. */
	public static final String SECTION_Parameter = "P";

	/** System section. */
	public static final String SECTION_System = "System";

	/** Branch section. */
	public static final String SECTION_Branch = "Branch";

	/** Line section. */
	public static final String SECTION_Line = "Line";

	/** Annotation section. */
	public static final String SECTION_Annotation = "Annotation";

	/** Object section. */
	public static final String SECTION_Object = "Object";

	/** Name parameter. */
	public static final String PARAM_Name = "Name";

	/** Ref parameter. */
	public static final String PARAM_Ref = "Ref";

	/** Slx model name parameter. */
	public static final String PARAM_SlxModelName = "model_";

	/** Class parameter. */
	public static final String PARAM_Class = "Class";

	/** Tree node parameter for parent relationship (Stateflow) */
	public static final String PARAM_treeNode = "treeNode";

	/** Link node parameter for parent relationship (Stateflow) */
	public static final String PARAM_linkNode = "linkNode";

	/** State label (Stateflow) */
	public static final String PARAM_labelString = "labelString";

	/** Junction type (Stateflow) */
	public static final String PARAM_type = "type";

	/** Machine parameter (Stateflow) */
	public static final String PARAM_machine = "machine";

	/** Icon shape. */
	public static final String PARAM_IconShape = "IconShape";

	/** Id (Stateflow) */
	public static final String PARAM_id = "id";

	/** Inputs */
	public static final String PARAM_Inputs = "Inputs";

	/** Name (Stateflow) */
	public static final String PARAM_name = "name";

	/** Points (used for lines). */
	public static final String PARAM_Points = "Points";

	/** Intersection (used for stateflow transitions). */
	public static final String PARAM_intersection = "intersection";

	/** Position (of blocks). */
	public static final String PARAM_Position = "Position";

	/** Foreground color (of blocks) */
	public static final String PARAM_ForegroundColor = "ForegroundColor";

	/** Background color (of blocks) */
	public static final String PARAM_BackgroundColor = "BackgroundColor";

	/** Block type parameter. */
	public static final String PARAM_BlockType = "BlockType";

	/** SID parameter. */
	public static final String PARAM_SID = "SID";

	/** SSID parameter. */
	public static final String PARAM_SSID = "SSID";

	/** Subviewer parameter. */
	public static final String PARAM_subviewer = "subviewer";

	/** PropName parameter. */
	public static final String PARAM_PropName = "PropName";

	/** ObjectID parameter. */
	public static final String PARAM_ObjectID = "ObjectID";

	/** ClassName parameter. */
	public static final String PARAM_ClassName = "ClassName";

	/** Type parameter. */
	public static final String PARAM_Type = "Type";

	/** Dimension parameter. */
	public static final String PARAM_Dimension = "Dimension";

	/** Destination block parameter. */
	public static final String PARAM_DstBlock = "DstBlock";

	/** Source parameter. */
	public static final String PARAM_Src = "Src";

	/** Source port parameter. */
	public static final String PARAM_SrcPort = "SrcPort";

	/** Destination parameter. */
	public static final String PARAM_Dst = "Dst";

	/** Destination port parameter. */
	public static final String PARAM_DstPort = "DstPort";

	/** Source block parameter. */
	public static final String PARAM_SrcBlock = "SrcBlock";

	/** Ports parameter. */
	public static final String PARAM_Ports = "Ports";

	/** Port parameter. */
	public static final String PARAM_Port = "Port";

	/** Targetlink data parameter. */
	public static final String PARAM_TARGETLINK_DATA = "data";

	/** The parameter that specifies the referenced type for a reference. */
	public static final String PARAM_SourceType = "SourceType";

	/** Mask value string parameter */
	public static final String PARAM_MaskValueString = "MaskValueString";

	/** Model name parameter. */
	public static final String PARAM_ModelName = "ModelName";

	/** Model name dialog parameter. */
	public static final String PARAM_ModelNameDialog = "ModelNameDialog";

	/** Source block parameter. */
	public static final String PARAM_SourceBlock = "SourceBlock";

	/** Value parameter. */
	public static final String PARAM_Value = "Value";

	/** "Simulink mask parameter" parameter used in objects. */
	public static final String PARAM_Simulink_MaskParameter = "Simulink.MaskParameter";

	/** Simulink mask parameter. */
	public static final String PARAM_Simulink_Mask = "Simulink.Mask";

	/** Parameter storing the computed model version. */
	public static final String PARAM_ComputedModelVersion = "ComputedModelVersion";

	/** Parameter storing the model's version. */
	public static final String PARAM_Version = "Version";

	/** Parameter for whether the name is shown. */
	public static final String PARAM_ShowName = "ShowName";

	/** Parameter for the name of the font used. */
	public static final String PARAM_FontName = "FontName";

	/** Parameter for the size of the font used. */
	public static final String PARAM_FontSize = "FontSize";

	/** Parameter for the weight of the font (i.e. bold). */
	public static final String PARAM_FontWeight = "FontWeight";

	/** Parameter for the angle of the font (i.e. italic). */
	public static final String PARAM_FontAngle = "FontAngle";

	/** Parameter for the placement of the name label. */
	public static final String PARAM_NamePlacement = "NamePlacement";

	/** Parameter for the labels of a line. */
	public static final String PARAM_Labels = "Labels";

	/** Parameter for drop shadow. */
	public static final String PARAM_DropShadow = "DropShadow";

	/** Parameter for block mirroring. */
	public static final String PARAM_BlockMirror = "BlockMirror";

	/** Parameter for block orientation. */
	public static final String PARAM_Orientation = "Orientation";

	/** Parameter for block rotation. */
	public static final String PARAM_BlockRotation = "BlockRotation";

	/** Parameter for port label visibility. */
	public static final String PARAM_ShowPortLabels = "ShowPortLabels";

	/** Simulink block type 'Abs'. */
	public static final String TYPE_Abs = "Abs";

	/** Simulink block type 'Assertion'. */
	public static final String TYPE_Assertion = "Assertion";

	/** Simulink block type 'Assignment'. */
	public static final String TYPE_Assignment = "Assignment";

	/** Simulink block type 'Backlash'. */
	public static final String TYPE_Backlash = "Backlash";

	/** Simulink block type 'Bias'. */
	public static final String TYPE_Bias = "Bias";

	/** Simulink block type 'BusAssignment'. */
	public static final String TYPE_BusAssignment = "BusAssignment";

	/** Simulink block type 'BusCreator'. */
	public static final String TYPE_BusCreator = "BusCreator";

	/** Simulink block type 'BusSelector'. */
	public static final String TYPE_BusSelector = "BusSelector";

	/** Simulink block type 'Clock'. */
	public static final String TYPE_Clock = "Clock";

	/** Simulink block type 'CombinatorialLogic'. */
	public static final String TYPE_CombinatorialLogic = "CombinatorialLogic";

	/** Simulink block type 'ComplexToMagnitudeAngle'. */
	public static final String TYPE_ComplexToMagnitudeAngle = "ComplexToMagnitudeAngle";

	/** Simulink block type 'ComplexToRealImag'. */
	public static final String TYPE_ComplexToRealImag = "ComplexToRealImag";

	/** Simulink block type 'Constant'. */
	public static final String TYPE_Constant = "Constant";

	/** Simulink block type 'DataStoreMemory'. */
	public static final String TYPE_DataStoreMemory = "DataStoreMemory";

	/** Simulink block type 'DataStoreRead'. */
	public static final String TYPE_DataStoreRead = "DataStoreRead";

	/** Simulink block type 'DataStoreWrite'. */
	public static final String TYPE_DataStoreWrite = "DataStoreWrite";

	/** Simulink block type 'DataTypeConversion'. */
	public static final String TYPE_DataTypeConversion = "DataTypeConversion";

	/** Simulink block type 'DeadZone'. */
	public static final String TYPE_DeadZone = "DeadZone";

	/** Simulink block type 'Demux'. */
	public static final String TYPE_Demux = "Demux";

	/** Simulink block type 'Derivative'. */
	public static final String TYPE_Derivative = "Derivative";

	/** Simulink block type 'DigitalClock'. */
	public static final String TYPE_DigitalClock = "DigitalClock";

	/** Simulink block type 'DiscreteFilter'. */
	public static final String TYPE_DiscreteFilter = "DiscreteFilter";

	/** Simulink block type 'DiscreteIntegrator'. */
	public static final String TYPE_DiscreteIntegrator = "DiscreteIntegrator";

	/** Simulink block type 'DiscretePulseGenerator'. */
	public static final String TYPE_DiscretePulseGenerator = "DiscretePulseGenerator";

	/** Simulink block type 'DiscreteStateSpace'. */
	public static final String TYPE_DiscreteStateSpace = "DiscreteStateSpace";

	/** Simulink block type 'DiscreteTransferFcn'. */
	public static final String TYPE_DiscreteTransferFcn = "DiscreteTransferFcn";

	/** Simulink block type 'DiscreteZeroPole'. */
	public static final String TYPE_DiscreteZeroPole = "DiscreteZeroPole";

	/** Simulink block type 'Display'. */
	public static final String TYPE_Display = "Display";

	/** Simulink block type 'EnablePort'. */
	public static final String TYPE_EnablePort = "EnablePort";

	/** Simulink block type 'Fcn'. */
	public static final String TYPE_Fcn = "Fcn";

	/** Simulink block type 'From'. */
	public static final String TYPE_From = "From";

	/** Simulink block type 'FromFile'. */
	public static final String TYPE_FromFile = "FromFile";

	/** Simulink block type 'FromWorkspace'. */
	public static final String TYPE_FromWorkspace = "FromWorkspace";

	/** Simulink block type 'Gain'. */
	public static final String TYPE_Gain = "Gain";

	/** Simulink block type 'Goto'. */
	public static final String TYPE_Goto = "Goto";

	/** Simulink block type 'GotoTagVisibility'. */
	public static final String TYPE_GotoTagVisibility = "GotoTagVisibility";

	/** Simulink block type 'Ground'. */
	public static final String TYPE_Ground = "Ground";

	/** Simulink block type 'HitCross'. */
	public static final String TYPE_HitCross = "HitCross";

	/** Simulink block type 'InitialCondition'. */
	public static final String TYPE_InitialCondition = "InitialCondition";

	/** Simulink block type 'Inport'. */
	public static final String TYPE_Inport = "Inport";

	/** Simulink block type 'Integrator'. */
	public static final String TYPE_Integrator = "Integrator";

	/** Simulink block type 'Logic'. */
	public static final String TYPE_Logic = "Logic";

	/** Simulink block type 'Lookup'. */
	public static final String TYPE_Lookup = "Lookup";

	/** Simulink block type 'Lookup2D'. */
	public static final String TYPE_Lookup2D = "Lookup2D";

	/** Simulink block type 'LookupND'. */
	public static final String TYPE_LookupND = "Lookup_n-D";

	/** Simulink block type 'M-S-Function'. */
	public static final String TYPE_M_S_Function = "M-S-Function";

	/** Simulink block type 'MATLABFcn'. */
	public static final String TYPE_MATLABFcn = "MATLABFcn";

	/** Simulink block type 'MagnitudeAngleToComplex'. */
	public static final String TYPE_MagnitudeAngleToComplex = "MagnitudeAngleToComplex";

	/** Simulink block type 'Math'. */
	public static final String TYPE_Math = "Math";

	/** Simulink block type 'Memory'. */
	public static final String TYPE_Memory = "Memory";

	/** Simulink block type 'Merge'. */
	public static final String TYPE_Merge = "Merge";

	/** Simulink block type 'MinMax'. */
	public static final String TYPE_MinMax = "MinMax";

	/** Simulink block type 'Model'. */
	public static final String TYPE_Model = "Model";

	/** Simulink block type 'MultiPortSwitch'. */
	public static final String TYPE_MultiPortSwitch = "MultiPortSwitch";

	/** Simulink block type 'Mux'. */
	public static final String TYPE_Mux = "Mux";

	/** Simulink block type 'Outport'. */
	public static final String TYPE_Outport = "Outport";

	/** Simulink block type 'Probe'. */
	public static final String TYPE_Probe = "Probe";

	/** Simulink block type 'Product'. */
	public static final String TYPE_Product = "Product";

	/** Simulink block type 'Quantizer'. */
	public static final String TYPE_Quantizer = "Quantizer";

	/** Simulink block type 'RandomNumber'. */
	public static final String TYPE_RandomNumber = "RandomNumber";

	/** Simulink block type 'RateLimiter'. */
	public static final String TYPE_RateLimiter = "RateLimiter";

	/** Simulink block type 'RateTransition'. */
	public static final String TYPE_RateTransition = "RateTransition";

	/** Simulink block type 'RealImagToComplex'. */
	public static final String TYPE_RealImagToComplex = "RealImagToComplex";

	/** Simulink block type 'Reference'. */
	public static final String TYPE_Reference = "Reference";

	/** Simulink block type 'RelationalOperator'. */
	public static final String TYPE_RelationalOperator = "RelationalOperator";

	/** Simulink block type 'Relay'. */
	public static final String TYPE_Relay = "Relay";

	/** Simulink block type 'Rounding'. */
	public static final String TYPE_Rounding = "Rounding";

	/** Simulink block type 'S-Function'. */
	public static final String TYPE_S_Function = "S-Function";

	/** Simulink block type 'Saturate'. */
	public static final String TYPE_Saturate = "Saturate";

	/** Simulink block type 'Scope'. */
	public static final String TYPE_Scope = "Scope";

	/** Simulink block type 'Selector'. */
	public static final String TYPE_Selector = "Selector";

	/** Simulink block type 'SignalConversion'. */
	public static final String TYPE_SignalConversion = "SignalConversion";

	/** Simulink block type 'SignalGenerator'. */
	public static final String TYPE_SignalGenerator = "SignalGenerator";

	/** Simulink block type 'SignalSpecification'. */
	public static final String TYPE_SignalSpecification = "SignalSpecification";

	/** Simulink block type 'Signum'. */
	public static final String TYPE_Signum = "Signum";

	/** Simulink block type 'Sin'. */
	public static final String TYPE_Sin = "Sin";

	/** Simulink block type 'StateSpace'. */
	public static final String TYPE_StateSpace = "StateSpace";

	/** Simulink block type 'Step'. */
	public static final String TYPE_Step = "Step";

	/** Simulink block type 'Stop'. */
	public static final String TYPE_Stop = "Stop";

	/** Simulink block type 'SubSystem'. */
	public static final String TYPE_SubSystem = "SubSystem";

	/** Simulink block type 'Sum'. */
	public static final String TYPE_Sum = "Sum";

	/** Simulink block type 'Switch'. */
	public static final String TYPE_Switch = "Switch";

	/** Simulink block type 'Terminator'. */
	public static final String TYPE_Terminator = "Terminator";

	/** Simulink block type 'ToFile'. */
	public static final String TYPE_ToFile = "ToFile";

	/** Simulink block type 'ToWorkspace'. */
	public static final String TYPE_ToWorkspace = "ToWorkspace";

	/** Simulink block type 'TransferFcn'. */
	public static final String TYPE_TransferFcn = "TransferFcn";

	/** Simulink block type 'TransportDelay'. */
	public static final String TYPE_TransportDelay = "TransportDelay";

	/** Simulink block type 'TriggerPort'. */
	public static final String TYPE_TriggerPort = "TriggerPort";

	/** Simulink block type 'Trigonometry'. */
	public static final String TYPE_Trigonometry = "Trigonometry";

	/** Simulink block type 'UniformRandomNumber'. */
	public static final String TYPE_UniformRandomNumber = "UniformRandomNumber";

	/** Simulink block type 'UnitDelay'. */
	public static final String TYPE_UnitDelay = "UnitDelay";

	/** Simulink block type 'VariableTransportDelay'. */
	public static final String TYPE_VariableTransportDelay = "VariableTransportDelay";

	/** Simulink block type 'Width'. */
	public static final String TYPE_Width = "Width";

	/** Simulink block type 'ZeroOrderHold'. */
	public static final String TYPE_ZeroOrderHold = "ZeroOrderHold";

	/** Simulink block type 'ZeroPole'. */
	public static final String TYPE_ZeroPole = "ZeroPole";

	/** Simulink port type 'trigger'. */
	public static final String TYPE_Trigger = "trigger";

	/** Simulink port type 'enable'. */
	public static final String TYPE_Enable = "enable";

	/** Simulink port type 'ifaction'. */
	public static final String TYPE_Ifaction = "ifaction";

	/** Simulink port type 'in'. */
	public static final String TYPE_In = "in";

	/** Simulink port type 'out'. */
	public static final String TYPE_Out = "out";

	/** Simulink type 'ModelReference'. */
	public static final String TYPE_ModelReference = "ModelReference";

	/**
	 * Simulink block name 'Subsystem' (Used by target link for structuring
	 * synthesized blocks)
	 **/
	public static final String NAME_Subsystem = "Subsystem";

	/** Round shape (for {@link #PARAM_IconShape}). */
	public static final String SHAPE_round = "round";

	/** Value for "off". */
	public static final String VALUE_off = "off";

	/** Value for "on". */
	public static final String VALUE_on = "on";

	/** Value for bold font. */
	public static final String VALUE_bold = "bold";

	/** Value for italic font. */
	public static final String VALUE_italic = "italic";

	/** Value for alternate placement. */
	public static final String VALUE_alternate = "alternate";

	/** Value for port label visibility "none". */
	public static final String VALUE_none = "none";

	/** Value for port label visibility "FromPortIcon". */
	public static final String VALUE_FromPortIcon = "FromPortIcon";

}