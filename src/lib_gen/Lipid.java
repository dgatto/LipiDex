package lib_gen;
import java.util.ArrayList;
import java.util.Collections;


public class Lipid extends Utilities
{
	ArrayList<FattyAcid> fattyAcids; 	//array of FAs
	LipidClass lClass; 					//Class
	Adduct adduct; 						//Adduct
	String formula; 					//Formula
	Double mass; 						//monoisotopic mass
	String polarity; 					//Polarity of lipid
	String name; 						//canonical name
	MS2 generatedMS2; 					//generated MS2
	ArrayList<String> uniqueTypes;		//Array of fa types this lipid can identify, used in LF

	//Constructor
	public Lipid (ArrayList<FattyAcid> fattyAcids, LipidClass lClass, Adduct adduct)
	{
		this.fattyAcids = fattyAcids;
		this.lClass = lClass;
		this.adduct = adduct;
		this.polarity = adduct.polarity;
		generateName();
		calculateFormula();
	}

	//Return mass
	public Double getMass()
	{
		return mass;
	}

	//Return adduct
	public Adduct getAdduct()
	{
		return adduct;
	}

	//Return generated MS2
	public MS2 getGeneratedMS2()
	{
		return generatedMS2;
	}

	//Return lipidClass object
	public LipidClass getLipidClass()
	{
		return lClass;
	}

	//Return arrayList of fatty acids in lipid
	public ArrayList<FattyAcid> getFattyAcids()
	{
		return fattyAcids;
	}

	//Add entry to unique FA type array
	public void addUniqueFAType(String faType)
	{
		if (uniqueTypes == null)
			uniqueTypes = new ArrayList<String>();

		uniqueTypes.add(faType);
	}

	//Get formula
	public String getFormula()
	{
		return formula;
	}

	//Get canonical name
	public String getName()
	{
		return name;
	}

	//Return adduct name as string
	public String getAdductName()
	{
		return adduct.getName();
	}

	//Adds a generator ms2 object
	public void addGeneratedMS2(MS2 ms2)
	{
		this.generatedMS2 = ms2;
	}

	//Sort FA Array
	public void sortFattyAcids()
	{
		Collections.sort(fattyAcids);
	}

	//Calculate Elemental Composition
	public void calculateFormula()
	{
		String tempFormula = "";

		//add in backbone + headgroup
		tempFormula = lClass.getFormula();

		//add in FAs
		for (int i=0; i<fattyAcids.size(); i++)
		{
			tempFormula =  mergeFormulas(tempFormula, fattyAcids.get(i).getFormula());
		}

		//add in adduct
		tempFormula =  mergeFormulas(tempFormula, adduct.getFormula());

		formula = tempFormula;

		calculateMass();
		
	}

	public String toString()
	{
		return getName();
	}

	//Calculate Monoisotopic mass
	public void calculateMass()
	{
		mass = calculateMassFromFormula(formula)/adduct.charge;
	}

	//Generate canonical name
	public void generateName()
	{
		//Add class name
		name = lClass.getAbbreviation()+" (";
		
		//Add fatty acids
		for (int i=0; i<fattyAcids.size(); i++)
		{
			name = name + fattyAcids.get(i).getName();
			if ((i+1)<fattyAcids.size()) name = name +"/";
		}

		name = name + ")";
	}

	private void formatItems() {
		
		// Separate molecules in chemical formula (i.e. -> H10 O2 C6)
		for (int i = 1; i < formula.length(); i++) {
		    char currentChar   = formula.charAt(i);
		    char behindOneChar = formula.charAt(i - 1);
		    char aheadOneChar;
		    // If last index of formula, then 'aheadOneChar' will be out of bounds, so we hardcode it to 'A'
		    if (i == formula.length() - 1) {
			    aheadOneChar = 'A';
		    } else {
		    	aheadOneChar = formula.charAt(i + 1);
		    }
		    
		    // If the current char is uppercase, the previous char was a digit, and the next char is not an 
		    // uppercase letter, then that signals the end of a molecule, so insert a space in between them
		    if (Character.isUpperCase(currentChar)) {
		    	if (Character.isDigit(behindOneChar) && !Character.isUpperCase(aheadOneChar)) {
		    		formula = formula.substring(0, i) + " " + formula.substring(i, formula.length());
		    	}
		    }    
		}
		
	}
	
	// Generate msp entry for library generation
	public String generateMSPResult()
	{
		String result = "";
		boolean optimalPolarity = false;
		
		formatItems();

		if (lClass.optimalPolarity.contains(this.polarity)) optimalPolarity = true;

		// Name field
		result += "Name: " + name + "\n";
		
		// Compound ID field
		result += "Compound ID: " + name + "\n";

		// Precursor Type field
		result += "Precursor_Type: " + adduct.getName() + "\n";
		
		// Precursor MZ field
		result += "PrecursorMZ: " + roundToFourDecimals(mass) + "\n";

		// Comment field
		result += "Comment: "+"Name="+name
				+" Mass="+roundToFourDecimals(mass)
				+" OptimalPolarity="+optimalPolarity
				+" Type=LipiDex"+"\n";
		
		// Formula field
		result += "Formula: " + formula + "\n";

		// NumPeaks field
		result += "Num Peaks: "+generatedMS2.getTransitions().size()+"\n";

		// MS2 array
		for (int i=0; i<generatedMS2.getTransitions().size(); i++)
		{
//			result += roundToFourDecimals(generatedMS2.getTransitions().get(i).getMass())+" "+
//					Math.round(generatedMS2.getTransitions().get(i).getIntensity())
//					+" \""+generatedMS2.getTransitions().get(i).getType()+"\"\n";
			
			result += roundToFourDecimals(generatedMS2.getTransitions().get(i).getMass()) + " " +
						Math.round(generatedMS2.getTransitions().get(i).getIntensity()) + "\n";
		}

		return result;
	}
}
