/**
 * 
 */
package rails.game.specific._1880;

import java.util.ArrayList;
import java.util.List;

import rails.algorithms.RevenueAdapter;
import rails.algorithms.RevenueStaticModifier;
import rails.common.parser.ConfigurationException;
import rails.common.parser.Tag;
import rails.common.GuiDef;
import rails.game.*;
import rails.game.model.ModelObject;
import rails.game.move.MoveableHolder;
import rails.game.move.RemoveFromList;
import rails.game.state.BooleanState;
import rails.game.state.IntegerState;


/**
 * @author Martin
 *
 */
public class PublicCompany_1880 extends PublicCompany implements RevenueStaticModifier {


    /** 
     *  Buildingrights belong to Phases in 1880 the player will be asked to choose which combination
     *   he wants and subsequently his company will be granted the appropriate rights. Further the value
     *  of the presidents share depends on the building right.
     *  A Player has the maximum of 3 phases without the use of a special power of a certain private paper.
     *  Example : A) Player chooses to build in Phase A+B+C (or B+C+D) this will lead to a president share value of 20%
     *            B) Player chooses to build in Phase A+B (or B+C or C+D) this will lead to a president share value of 30 %
     *            C) Player chooses to build in Phase A (or B or C or D) this will lead to a president share value of 40 %
     *    The BitSet BuildingRights should be able to handle the information :
     *    Bit 1 set True Player can build in Phase A
     *    Bit 2 set True Player can build in Phase B
     *    Bit 3 set True Player can build in Phase C
     *    Bit 4 set True Player can build in Phase D
     *    
     */
    private BuildingRights_1880 buildingRights = new BuildingRights_1880("buildingRights"); 
   
    //Implementation of PhaseAction to be able to handle the CommunistPhase
    private BooleanState communistTakeOver = new BooleanState ("communistTakeOver",false);
        
    //Implementation of Phase Action to be able to handle the Post Communist Phase
    private BooleanState shanghaiExchangeFounded = new BooleanState ("shanghaiExchangeFounded",false);
    
    private BooleanState allCertsAvail = new BooleanState ("allCertsAvail", false);
    
    private boolean fullyCapitalized = false;
    private boolean fullCapitalAvailable = false;
    private int extraCapital = 0;
    
    protected IntegerState formationOrderIndex;
    
    protected IntegerState operationSlotIndex = new IntegerState ("OperatingSlot, 0");
  
    /**
     * 
     */
    public PublicCompany_1880() {
        super();
    }
    
    public void start(StockSpaceI startSpace) {
        extraCapital = 5 * (startSpace.getPrice());
        super.start(startSpace);
    }

    /* (non-Javadoc)
     * @see rails.game.PublicCompany#configureFromXML(rails.common.parser.Tag)
     */
    @Override
    public void configureFromXML(Tag tag) throws ConfigurationException {
        super.configureFromXML(tag);
    }
    
    
    /* (non-Javadoc)
     * @see rails.game.PublicCompany#finishConfiguration(rails.game.GameManagerI)
     */
    @Override
    public void finishConfiguration(GameManagerI gameManager)
            throws ConfigurationException {
        super.finishConfiguration(gameManager);
        gameManager.setGuiParameter (GuiDef.Parm.HAS_ANY_RIGHTS, true);
        gameManager.getRevenueManager().addStaticModifier(this);
    }

    /**
     * @param buildingRights the buildingRights to set
     */
    public void setBuildingRights(String buildingRights) {
        this.buildingRights.set(buildingRights);
    }

    public void setCommunistTakeOver(boolean b) {
        communistTakeOver.set(b);
        
    }
    /**
     * @return the communistTakeOver
     */
    public Boolean isCommunistPhase() {
        return communistTakeOver.booleanValue();
    }
    
    public ModelObject getCommunistTakeOver() {
        return communistTakeOver;
    }

    public boolean modifyCalculator(RevenueAdapter revenueAdapter) {
        return false;
    }

    /** Don't move the space if the company is withholding train income during the CommunistPhase
     * 
     */
    @Override
    public void withhold(int amount) {
        if (isCommunistPhase()) return;
        if (hasStockPrice) stockMarket.withhold(this); // TODO: Cleanup
    }

    public void setFloatPercentage(int i) {
        this.floatPerc=i;
        
    }
    
    @Override
    public boolean canRunTrains() {
        if (!isCommunistPhase() && (!hasStockPrice()) ){ // TODO: Cleanup
            return true;
            }
        return portfolio.getNumberOfTrains() > 0;
       
    }
    
    
    
    /* (non-Javadoc)
     * @see rails.game.PublicCompany#getNumberOfTileLays(java.lang.String)
     */
    @Override
    public int getNumberOfTileLays(String tileColour) {
        PhaseI phase = gameManager.getPhaseManager().getCurrentPhase();
        
         if ((tileColour.equals("yellow")) && (this.getName().equals("BCR"))) {
             return 2;
         }
         int tileLays = phase.getTileLaysPerColour(getTypeName(), tileColour);
             if (tileLays <= 1) {
                 extraTileLays = null;
                 return tileLays;
                 }
            // More than one tile lay allowed.
             return tileLays;
     }

    /**
     * @return the shanghaiExchangeFounded
     */
    public BooleanState getShanghaiExchangeFounded() {
        return shanghaiExchangeFounded;
    }

    /**
     * @param shanghaiExchangeFounded the shanghaiExchangeFounded to set
     */
    public void setShanghaiExchangeFounded(BooleanState shanghaiExchangeFounded) {
        this.shanghaiExchangeFounded = shanghaiExchangeFounded;
    }

    public boolean shanghaiExchangeIsOperational(){
        return this.shanghaiExchangeFounded.booleanValue();
    }
    
    /* (non-Javadoc)
     * @see rails.algorithms.RevenueStaticModifier#prettyPrint(rails.algorithms.RevenueAdapter)
     */
    public String prettyPrint(RevenueAdapter revenueAdapter) {
        return null;
    }
    
    /*
     * @param Phase
     */
    public boolean hasBuildingRightForPhase(PhaseI phase) {
        return buildingRights.canBuildInPhase(phase);
    }
    
    /*
     * If we have a different president share percentage we have to remove the old certificate structure 
     * and rebuild a new structure. There will be no subsequent certificate alterations in 1880.
     * 
     * @author Martin Brumm
     * @param percentage
     * 
     * To be called from the StartRound_1880 / StockRoundWindow_1880
     */
    
    public void setPresidentShares(int percentage) {
        
        List<PublicCertificateI>certs = new ArrayList<PublicCertificateI>(certificates);
        int share = 0;
        
        //Create a new President Certificate with the shares (percentage)
        PublicCertificateI certificate = new PublicCertificate((percentage/10), true,
                true, 1.0f, 0);
        
        //we need to bring that Certificate to the List, do we have to place it at a specific place ? I hope not...
        MoveableHolder scrapHeap = bank.getScrapHeap();
        for (PublicCertificateI cert : certs) {
            if (cert.isPresidentShare()) { // get the president share and remove that...
                cert.moveTo(scrapHeap);
                new RemoveFromList<PublicCertificateI>(certificates, cert, this.name+"_certs");
            } else if (share >= (100-(percentage) )) {
                    cert.moveTo(scrapHeap);
                    new RemoveFromList<PublicCertificateI>(certificates, cert, this.name+"_certs");
            } else {
                    cert.setCertificateCount(1.0f);
                    share += cert.getShare();
            }
             
        }
        //Now add the new president share to the list ; do we have to call namecertificates ?
        
        certificates.add(0,certificate); //Need to make sure the new share is at position 0 !
        nameCertificates(); //Just to be sure..
        PublicCertificateI cert;
        for (int i = 0; i < certificates.size(); i++) {
            cert = certificates.get(i);
            cert.setUniqueId(name, i);
            cert.setInitiallyAvailable(cert.isInitiallyAvailable());
        }
        
          MoveableHolder bankIPO= bank.getIpo();
          certificate.moveTo(bankIPO);
        
       /*     // Update all owner ShareModels (once)
        *    // to have the UI get the correct percentage
        * // Martin: perhaps this not necessary as the certificates in question havent been handed out...
        */
           List<Portfolio> done = new ArrayList<Portfolio>();
            Portfolio portfolio;
            for (PublicCertificateI cert2 : certificates) {
              portfolio = (Portfolio)cert2.getHolder();
              if (!done.contains(portfolio)) {
                 portfolio.getShareModel(this).setShare();
                  done.add(portfolio);
              }
           }
       
    }

    /**
     * @return the fullyCapitalised
     */
    public boolean isFullyCapitalized() {
        return fullyCapitalized;
    }

    /**
     * @param fullyCapitalised the fullyCapitalised to set
     */
    public void setFullyCapitalized(boolean fullyCapitalised) {
        this.fullyCapitalized = fullyCapitalised;
    }

    /**
     * @return the allCertsAvail
     */
    public boolean getAllCertsAvail() {
        return allCertsAvail.booleanValue();
    }

    /**
     * @param flag the allCertsAvail to set
     */
    public void setAllCertsAvail(boolean flag ) {
        this.allCertsAvail.set(flag);
    }
    
    public boolean certsAvailableForSale() {
        if ((sharesInIpo() == 5) && (allCertsAvail.booleanValue() == false)) {
            return false;
        } else if (sharesInIpo() == 0) {
            return false;
        }
        return true;
    }
    
    private int sharesInIpo() {
        int sharesInIpo = 0;
        for (PublicCertificateI cert : certificates) {
            if (cert.getPortfolio().getOwner() instanceof Bank) {
                sharesInIpo += cert.getShares();
            }
        }
        return sharesInIpo;
    }
    
    public List<TokenI> getLaidBaseTokens() {
        return laidBaseTokens;
    }

    public void setFullFundingAvail() {
        this.fullCapitalAvailable = true;
        checkToFullyCapitalize();
    }

    public void sharePurchased() {
        if (fullyCapitalized == true) {
            return;
        }
        checkToFullyCapitalize();
    }
    
    private void checkToFullyCapitalize() {
        if ((hasFloated() == true) && (sharesInIpo() <= 5) && (fullCapitalAvailable == true)) {
            fullyCapitalized = true;
            addCash(extraCapital);  // TODO: Should this be a "MOVE" instead?
        }
    }
    
    public ModelObject getRightsModel () {
        return buildingRights;
    }
    
    static public List<PublicCompany_1880> getPublicCompanies(CompanyManagerI companyManager) {
        List<PublicCompany_1880> companies = new ArrayList<PublicCompany_1880>();
        for (PublicCompanyI company : companyManager.getAllPublicCompanies()) {
            if (company instanceof PublicCompany_1880) {
                companies.add((PublicCompany_1880) company);
            }
        }
        return companies;
    }

}
