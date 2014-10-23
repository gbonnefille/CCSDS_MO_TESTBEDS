/*******************************************************************************
 * Copyright or � or Copr. CNES
 *
 * This software is a computer program whose purpose is to provide a 
 * framework for the CCSDS Mission Operations services.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package org.ccsds.moims.mo.malspp.test.patterns.pubsub;

import java.util.Hashtable;
import java.util.Map;

import org.ccsds.moims.mo.mal.MALException;
import org.ccsds.moims.mo.mal.MALHelper;
import org.ccsds.moims.mo.mal.MALInteractionException;
import org.ccsds.moims.mo.mal.MALStandardError;
import org.ccsds.moims.mo.mal.provider.MALPublishInteractionListener;
import org.ccsds.moims.mo.mal.structures.EntityKeyList;
import org.ccsds.moims.mo.mal.structures.EntityRequestList;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.QoSLevel;
import org.ccsds.moims.mo.mal.structures.SessionType;
import org.ccsds.moims.mo.mal.structures.Subscription;
import org.ccsds.moims.mo.mal.structures.UInteger;
import org.ccsds.moims.mo.mal.structures.UpdateHeaderList;
import org.ccsds.moims.mo.mal.test.patterns.pubsub.HeaderTestProcedure;
import org.ccsds.moims.mo.mal.test.patterns.pubsub.HeaderTestProcedureImpl;
import org.ccsds.moims.mo.mal.test.patterns.pubsub.PubSubTestCaseHelper;
import org.ccsds.moims.mo.mal.test.suite.LocalMALInstance.IPTestConsumer;
import org.ccsds.moims.mo.mal.transport.MALErrorBody;
import org.ccsds.moims.mo.mal.transport.MALMessageHeader;
import org.ccsds.moims.mo.malprototype.iptest.IPTestHelper;
import org.ccsds.moims.mo.malprototype.iptest.consumer.IPTest;
import org.ccsds.moims.mo.malprototype.iptest.consumer.IPTestAdapter;
import org.ccsds.moims.mo.malprototype.iptest.provider.MonitorPublisher;
import org.ccsds.moims.mo.malprototype.iptest.structures.TestPublishUpdate;
import org.ccsds.moims.mo.malprototype.iptest.structures.TestUpdateList;
import org.ccsds.moims.mo.malspp.test.patterns.SpacePacketCheck;
import org.ccsds.moims.mo.malspp.test.suite.ErrorBrokerHandler;
import org.ccsds.moims.mo.malspp.test.suite.LocalMALInstance;
import org.ccsds.moims.mo.malspp.test.suite.PubsubErrorIPTestHandler;
import org.ccsds.moims.mo.malspp.test.suite.TestServiceProvider;
import org.ccsds.moims.mo.testbed.util.FileBasedDirectory;
import org.ccsds.moims.mo.testbed.util.ParseHelper;
import org.objectweb.util.monolog.api.Logger;

public class MalSppPubsubTest extends HeaderTestProcedureImpl {
	
	public final static Logger logger = fr.dyade.aaa.common.Debug
		  .getLogger(MalSppPubsubTest.class.getName());
	
  private SpacePacketCheck spacePacketCheck = new SpacePacketCheck();
  
  public boolean consumerPacketIsTc(boolean isTc) {
    return spacePacketCheck.consumerPacketIsTc(isTc);
  }
  
  public boolean providerPacketIsTc(boolean isTc) {
    return spacePacketCheck.providerPacketIsTc(isTc);
  }
  
  public FileBasedDirectory.URIpair getProviderURIs(boolean shared) {
    FileBasedDirectory.URIpair uris;
    int consumerPacketType = spacePacketCheck.getConsumerPacketType();
    int providerPacketType = spacePacketCheck.getProviderPacketType();
    if (consumerPacketType == 1) {
      if (providerPacketType == 1) {
        if (shared) {
          uris = FileBasedDirectory
              .loadURIs(TestServiceProvider.IP_TEST_PROVIDER_WITH_SHARED_BROKER_NAME);
        } else {
          uris = FileBasedDirectory.loadURIs(IPTestHelper.IPTEST_SERVICE_NAME
              .getValue());
        }
      } else {
        if (shared) {
          uris = FileBasedDirectory
              .loadURIs(TestServiceProvider.TC_TM_IP_TEST_PROVIDER_WITH_SHARED_BROKER_NAME);
        } else {
          uris = FileBasedDirectory.loadURIs(TestServiceProvider.TM_IP_TEST_PROVIDER_NAME);
        }
      }
    } else {
      if (providerPacketType == 1) {
        if (shared) {
          uris = FileBasedDirectory
              .loadURIs(TestServiceProvider.TM_TC_IP_TEST_PROVIDER_WITH_SHARED_BROKER_NAME);
        } else {
          uris = FileBasedDirectory.loadURIs(IPTestHelper.IPTEST_SERVICE_NAME
              .getValue());
        }
      } else {
        if (shared) {
          uris = FileBasedDirectory
              .loadURIs(TestServiceProvider.TM_TM_IP_TEST_PROVIDER_WITH_SHARED_BROKER_NAME);
        } else {
          uris = FileBasedDirectory.loadURIs(TestServiceProvider.TM_IP_TEST_PROVIDER_NAME);
        }
      }
    }
    return uris;
  }
  
  protected void initConsumer(int domain, SessionType session, Identifier sessionName,
      QoSLevel qos, boolean shared) throws Exception {
    int consumerPacketType = spacePacketCheck.getConsumerPacketType();
    int providerPacketType = spacePacketCheck.getProviderPacketType();
    if (consumerPacketType == 1) {
      if (providerPacketType == 1) {
        ipTestConsumer = LocalMALInstance.instance().getTcTcIpTestStub(
            HeaderTestProcedure.AUTHENTICATION_ID,
            HeaderTestProcedure.getDomain(domain),
            HeaderTestProcedure.NETWORK_ZONE, session, sessionName, qos,
            HeaderTestProcedure.PRIORITY, shared);
      } else {
        ipTestConsumer = LocalMALInstance.instance().getTcTmIpTestStub(
            HeaderTestProcedure.AUTHENTICATION_ID, HeaderTestProcedure.getDomain(domain),
            HeaderTestProcedure.NETWORK_ZONE, session, sessionName, qos,
            HeaderTestProcedure.PRIORITY, shared);
      }
    } else {
      if (providerPacketType == 1) {
        ipTestConsumer = LocalMALInstance.instance().getTmTcIpTestStub(
            HeaderTestProcedure.AUTHENTICATION_ID,
            HeaderTestProcedure.getDomain(domain),
            HeaderTestProcedure.NETWORK_ZONE, session, sessionName, qos,
            HeaderTestProcedure.PRIORITY, shared);
      } else {
        ipTestConsumer = LocalMALInstance.instance().getTmTmIpTestStub(
            HeaderTestProcedure.AUTHENTICATION_ID, HeaderTestProcedure.getDomain(domain),
            HeaderTestProcedure.NETWORK_ZONE, session, sessionName, qos,
            HeaderTestProcedure.PRIORITY, shared);
      }
    }
  }
  
	public boolean selectReceivedPacketAt(int index) {
		return spacePacketCheck.selectReceivedPacketAt(index);
	}
	
	public boolean selectSentPacketAt(int index) {
		return spacePacketCheck.selectSentPacketAt(index);
	}
	
	public boolean checkTimestamp() throws Exception {
		return spacePacketCheck.checkTimestamp();
	}
	
	public boolean checkSpacePacketType() {
		return spacePacketCheck.checkSpacePacketType();
  }
	
	public int versionIs() {
		return spacePacketCheck.versionIs();
	}
	
	public int sduTypeIs() {
		return spacePacketCheck.sduTypeIs();
  }
	
	public int areaIs() {
		return spacePacketCheck.areaIs();
  }
	
	public int serviceIs() {
		return spacePacketCheck.serviceIs();
  }
	
	public int operationIs() {
		return spacePacketCheck.operationIs();
  }
	
	public int areaVersionIs() {
		return spacePacketCheck.areaVersionIs();
  }
	
	public int errorFlagIs() {
		return spacePacketCheck.errorFlagIs();
  }
	
	public boolean checkUriFrom() {
		return spacePacketCheck.checkUriFrom();
	}
	
	public boolean checkUriTo() {
		return spacePacketCheck.checkUriTo();
	}
	
	public boolean checkTransactionId() {
		return spacePacketCheck.checkTransactionId();
	}
	
	public boolean resetSppInterceptor() {
		return spacePacketCheck.resetSppInterceptor();
	}
	
	public boolean checkQos(String qosLevelAsString) throws Exception {
		return spacePacketCheck.checkQos(qosLevelAsString);
	}
	
	public boolean checkSession(String sessionTypeAsString) throws Exception {
		return spacePacketCheck.checkSession(sessionTypeAsString);
  }
	
	public long priorityIs() throws Exception {
		return spacePacketCheck.priorityIs();
	}
	
	public String networkZoneIs() throws Exception {
		return spacePacketCheck.networkZoneIs();
	}
	
	public String sessionNameIs() throws Exception {
		return spacePacketCheck.sessionNameIs();
	}

	public boolean checkAuthenticationId() throws Exception {
		return spacePacketCheck.checkAuthenticationId();
	}
	
	public boolean checkDomainId() throws Exception {
		return spacePacketCheck.checkDomainId();
	}
  
  public boolean checkSecondaryApid() {
    return spacePacketCheck.checkSecondaryApid();
  }
  
  public boolean checkSecondaryApidQualifier() {
    return spacePacketCheck.checkSecondaryApidQualifier();
  }
  
  public byte sourceIdFlagIs() throws Exception {
    return spacePacketCheck.sourceIdFlagIs();
  }
  
  public byte destinationIdFlagIs() throws Exception {
    return spacePacketCheck.destinationIdFlagIs();
  }
  
  public byte priorityFlagIs() throws Exception {
    return spacePacketCheck.priorityFlagIs();
  }
  
  public byte timestampFlagIs() throws Exception {
    return spacePacketCheck.timestampFlagIs();
  }
  
  public byte networkZoneFlagIs() throws Exception {
    return spacePacketCheck.networkZoneFlagIs();
  }
  
  public byte sessionNameFlagIs() throws Exception {
    return spacePacketCheck.sessionNameFlagIs();
  }
  
  public byte domainFlagIs() throws Exception {
    return spacePacketCheck.domainFlagIs();
  }
  
  public byte authenticationIdFlagIs() throws Exception {
    return spacePacketCheck.authenticationIdFlagIs();
  }
  
  public boolean readSourceId() {
    return spacePacketCheck.readSourceId();
  }
  
  public boolean readDestinationId() {
    return spacePacketCheck.readDestinationId();
  }
  
  public int segmentCounterIs() {
    return spacePacketCheck.segmentCounterIs();
  }
  
  private IPTestConsumer getPubsubErrorIPTestStub(int domain, SessionType session,
      Identifier sessionName, QoSLevel qos) throws Exception {
    int consumerPacketType = spacePacketCheck.getConsumerPacketType();
    int providerPacketType = spacePacketCheck.getProviderPacketType();
    if (consumerPacketType == 1) {
      if (providerPacketType == 1) {
        return LocalMALInstance.instance().getTcTcPubsubErrorIPTestStub(
            HeaderTestProcedure.AUTHENTICATION_ID,
            HeaderTestProcedure.getDomain(domain),
            HeaderTestProcedure.NETWORK_ZONE, session, sessionName, qos,
            HeaderTestProcedure.PRIORITY);
      } else {
        return LocalMALInstance.instance()
            .getTcTmPubsubErrorIPTestStub(
                HeaderTestProcedure.AUTHENTICATION_ID,
                HeaderTestProcedure.getDomain(domain),
                HeaderTestProcedure.NETWORK_ZONE, session, sessionName, qos,
                HeaderTestProcedure.PRIORITY);
      }
    } else {
      if (providerPacketType == 1) {
        return LocalMALInstance.instance()
            .getTmTcPubsubErrorIPTestStub(
                HeaderTestProcedure.AUTHENTICATION_ID,
                HeaderTestProcedure.getDomain(domain),
                HeaderTestProcedure.NETWORK_ZONE, session, sessionName, qos,
                HeaderTestProcedure.PRIORITY);
      } else {
        return LocalMALInstance.instance()
            .getTmTmPubsubErrorIPTestStub(
                HeaderTestProcedure.AUTHENTICATION_ID,
                HeaderTestProcedure.getDomain(domain),
                HeaderTestProcedure.NETWORK_ZONE, session, sessionName, qos,
                HeaderTestProcedure.PRIORITY);
      }
    } 
  }

	public boolean initiateRegisterErrorWithQosAndSessionAndDomain(String qosLevel,
      String sessionType, int domain) throws Exception
  {
    logMessage("initiateRegisterErrorWithQosAndSessionAndDomain(" + qosLevel + ',' + sessionType + ',' + domain+ ')');
    
    QoSLevel qos = ParseHelper.parseQoSLevel(qosLevel);
    SessionType session = ParseHelper.parseSessionType(sessionType);
    Identifier sessionName = PubSubTestCaseHelper.getSessionName(session);
    
    IPTestConsumer ipTestConsumer = getPubsubErrorIPTestStub(domain, session, sessionName, qos);
    IPTest ipTest = ipTestConsumer.getStub();
    
    Subscription subscription = new Subscription(new Identifier(
        ErrorBrokerHandler.SUBSCRIPTION_RAISING_ERROR), new EntityRequestList());

    try {
      ipTest.monitorRegister(subscription, new IPTestListener());
    } catch (MALInteractionException exc) {
      // Expected error
    	return true;
    }

    Thread.sleep(2000);
    
    return false;
  }
	
	public boolean initiateNotifyErrorWithQosAndSessionAndDomain(String qosLevel,
      String sessionType, int domain) throws Exception
  {
    logMessage("initiateNotifyErrorWithQosAndSessionAndDomain(" + qosLevel + ',' + sessionType + ',' + domain+ ')');
    
    QoSLevel qos = ParseHelper.parseQoSLevel(qosLevel);
    SessionType session = ParseHelper.parseSessionType(sessionType);
    Identifier sessionName = PubSubTestCaseHelper.getSessionName(session);
    
    // First, need to subscribe in order to synchronize with the Notify  
    IPTestConsumer ipTestConsumer = getPubsubErrorIPTestStub(domain, session, sessionName, qos);
    IPTest ipTest = ipTestConsumer.getStub();
    
    Subscription subscription = new Subscription(new Identifier("subscription"), new EntityRequestList());
    
    IPTestListener listener = new IPTestListener();
    
    try {
      ipTest.monitorRegister(subscription, listener);
    } catch (MALInteractionException exc) {
      // Unexpected error
      logMessage("Unexpected error: " + exc);
      return false;
    }
    
    //IPTestConsumer errorIpTestConsumer = getPubsubErrorIPTestStub(domain, session, sessionName, qos);
    //IPTest errorIpTest = errorIpTestConsumer.getStub();
    
    UInteger errorCode = MALHelper.INTERNAL_ERROR_NUMBER;
    TestPublishUpdate testPublishUpdate = new TestPublishUpdate(qos, HeaderTestProcedure.PRIORITY, 
    		HeaderTestProcedure.getDomain(domain), 
    		HeaderTestProcedure.NETWORK_ZONE, session, sessionName, false, new UpdateHeaderList(), 
        new TestUpdateList(), errorCode, Boolean.FALSE, null);
    ipTest.publishUpdates(testPublishUpdate);
    
    listener.waitNotifyError();

    return true;
  }
	
	private PubsubErrorIPTestHandler initHandlerForPublishRegister() throws Exception {
    int consumerPacketType = spacePacketCheck.getConsumerPacketType();
    int providerPacketType = spacePacketCheck.getProviderPacketType();
    if (consumerPacketType == 1) {
      if (providerPacketType == 1) {
        return LocalMALInstance.instance().getTcTcHandlerForPublishRegister();
      } else {
        return LocalMALInstance.instance().getTcTmHandlerForPublishRegister();
      }
    } else {
      if (providerPacketType == 1) {
        return LocalMALInstance.instance().getTmTcHandlerForPublishRegister();
      } else {
        return LocalMALInstance.instance().getTmTmHandlerForPublishRegister();
      }
    } 
  }
	
	public boolean initiatePublishRegisterErrorWithQosAndSessionAndDomain(String qosLevel,
      String sessionType, int domain) throws Exception
  {
    logMessage("initiatePublishRegisterErrorWithQosAndSessionAndDomain(" + qosLevel + ',' + sessionType + ',' + domain+ ')');
    
    PubsubErrorIPTestHandler ipTestHandler = initHandlerForPublishRegister();
    
    QoSLevel qos = ParseHelper.parseQoSLevel(qosLevel);
    SessionType session = ParseHelper.parseSessionType(sessionType);
    Identifier sessionName = PubSubTestCaseHelper.getSessionName(session);
    MonitorPublisher publisher = ipTestHandler.createMonitorPublisher(
    		HeaderTestProcedure.getDomain(domain), 
    		HeaderTestProcedure.NETWORK_ZONE, session,
			  sessionName, qos, new Hashtable(), new UInteger(1));
	 
    try {
      publisher.register(new EntityKeyList(), new PublishListener());
    } catch (MALInteractionException exc) {
    	// Expected error
  	  return true;
    }

    return false;
  }
	
	static class IPTestListener extends IPTestAdapter {
	  
	  private boolean notifyErrorReceived;
	  
    @Override
    public synchronized void monitorNotifyErrorReceived(MALMessageHeader msgHeader,
        MALStandardError error, Map qosProperties) {
      System.out.println("monitorNotifyErrorReceived: " + error);
      notifyErrorReceived = true;
      notify();
    }
    
    synchronized void waitNotifyError() {
      while (! notifyErrorReceived) {
        try {
          wait();
        } catch (InterruptedException e) {}
      }
    }

	}
	
	static class PublishListener implements MALPublishInteractionListener {

		public void publishDeregisterAckReceived(MALMessageHeader arg0, Map arg1)
        throws MALException {
	    // TODO Auto-generated method stub
	    
    }

		public void publishErrorReceived(MALMessageHeader arg0, MALErrorBody arg1,
        Map arg2) throws MALException {
	    // TODO Auto-generated method stub
	    
    }

		public void publishRegisterAckReceived(MALMessageHeader arg0, Map arg1)
        throws MALException {
	    // TODO Auto-generated method stub
	    
    }

		public void publishRegisterErrorReceived(MALMessageHeader arg0,
        MALErrorBody arg1, Map arg2) throws MALException {
	    // TODO Auto-generated method stub
	    
    }
		
	}
	
}
