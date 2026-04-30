package ch.epfl.sigcheck;

// Attention : cette classe n'est *pas* un test JUnit, et son code n'est pas
// destiné à être exécuté. Son seul but est de vérifier, autant que possible,
// que les noms et les types des différentes entités à définir pour cette
// étape du projet sont corrects.

import ch.epfl.rechor.journey.Journey;
import ch.epfl.rechor.journey.Stop;
import ch.epfl.rechor.journey.Vehicle;

final class SignatureChecks_1 {
    private SignatureChecks_1() {}

    void checkPreconditions() {
        ch.epfl.rechor.Preconditions.checkArgument(v01);
    }

    void checkVehicle() {
        v03 = (java.lang.Enum) v02;
        v02 = Vehicle.AERIAL_LIFT;
        v04 = Vehicle.ALL;
        v02 = Vehicle.BUS;
        v02 = Vehicle.FERRY;
        v02 = Vehicle.FUNICULAR;
        v02 = Vehicle.METRO;
        v02 = Vehicle.TRAIN;
        v02 = Vehicle.TRAM;
        v02 = Vehicle.valueOf(v05);
        v06 = Vehicle.values();
    }

    void checkStop() {
        v03 = (java.lang.Record) v07;
        v07 = new Stop(v05, v05, v08, v08);
        v01 = v07.equals(v03);
        v09 = v07.hashCode();
        v08 = v07.latitude();
        v08 = v07.longitude();
        v05 = v07.name();
        v05 = v07.platformName();
        v05 = v07.toString();
    }

    void checkJourney() {
        v03 = (java.lang.Record) v10;
        v10 = new Journey(v11);
        v07 = v10.arrStop();
        v12 = v10.arrTime();
        v07 = v10.depStop();
        v12 = v10.depTime();
        v13 = v10.duration();
        v01 = v10.equals(v03);
        v09 = v10.hashCode();
        v11 = v10.legs();
        v05 = v10.toString();
    }

    void checkJourney_Leg() {
        v07 = v14.arrStop();
        v12 = v14.arrTime();
        v07 = v14.depStop();
        v12 = v14.depTime();
        v13 = v14.duration();
        v15 = v14.intermediateStops();
    }

    void checkJourney_Leg_Foot() {
        v03 = (java.lang.Record) v16;
        v03 = (Journey.Leg) v16;
        v16 = new Journey.Leg.Foot(v07, v12, v07, v12);
        v07 = v16.arrStop();
        v12 = v16.arrTime();
        v07 = v16.depStop();
        v12 = v16.depTime();
        v01 = v16.equals(v03);
        v09 = v16.hashCode();
        v15 = v16.intermediateStops();
        v01 = v16.isTransfer();
        v05 = v16.toString();
    }

    void checkJourney_Leg_Transport() {
        v03 = (java.lang.Record) v17;
        v03 = (Journey.Leg) v17;
        v17 = new Journey.Leg.Transport(v07, v12, v07, v12, v15, v02, v05, v05);
        v07 = v17.arrStop();
        v12 = v17.arrTime();
        v07 = v17.depStop();
        v12 = v17.depTime();
        v05 = v17.destination();
        v01 = v17.equals(v03);
        v09 = v17.hashCode();
        v15 = v17.intermediateStops();
        v05 = v17.route();
        v05 = v17.toString();
        v02 = v17.vehicle();
    }

    void checkJourney_Leg_IntermediateStop() {
        v03 = (java.lang.Record) v18;
        v18 = new Journey.Leg.IntermediateStop(v07, v12, v12);
        v12 = v18.arrTime();
        v12 = v18.depTime();
        v01 = v18.equals(v03);
        v09 = v18.hashCode();
        v07 = v18.stop();
        v05 = v18.toString();
    }

    void checkFormatterFr() {
        v05 = ch.epfl.rechor.FormatterFr.formatDuration(v13);
        v05 = ch.epfl.rechor.FormatterFr.formatLeg(v17);
        v05 = ch.epfl.rechor.FormatterFr.formatLeg(v16);
        v05 = ch.epfl.rechor.FormatterFr.formatPlatformName(v07);
        v05 = ch.epfl.rechor.FormatterFr.formatRouteDestination(v17);
        v05 = ch.epfl.rechor.FormatterFr.formatTime(v12);
    }

    boolean v01;
    Vehicle v02;
    java.lang.Object v03;
    java.util.List<Vehicle> v04;
    java.lang.String v05;
    Vehicle[] v06;
    Stop v07;
    double v08;
    int v09;
    Journey v10;
    java.util.List<Journey.Leg> v11;
    java.time.LocalDateTime v12;
    java.time.Duration v13;
    Journey.Leg v14;
    java.util.List<Journey.Leg.IntermediateStop> v15;
    Journey.Leg.Foot v16;
    Journey.Leg.Transport v17;
    Journey.Leg.IntermediateStop v18;
}
