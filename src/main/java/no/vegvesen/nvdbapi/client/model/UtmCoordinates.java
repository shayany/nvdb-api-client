/*
 * Copyright (c) 2015-2017, Statens vegvesen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.vegvesen.nvdbapi.client.model;

import java.io.Serializable;
import java.util.Objects;

public final class UtmCoordinates implements Serializable {
    private final Double northing;
    private final Double easting;
    private final Double altitude;

    public UtmCoordinates(Double easting, Double northing, Double altitude) {
        this.northing = Objects.requireNonNull(northing);
        this.easting = Objects.requireNonNull(easting);
        this.altitude = Objects.requireNonNull(altitude);
    }

    public Double getNorthing() {
        return northing;
    }

    public Double getEasting() {
        return easting;
    }

    public Double getAltitude() {
        return altitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtmCoordinates that = (UtmCoordinates) o;
        return Objects.equals(northing, that.northing) &&
            Objects.equals(easting, that.easting) &&
            Objects.equals(altitude, that.altitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(northing, easting, altitude);
    }

    @Override
    public String toString() {
        return String.format("[%s,%s,%s]", easting, northing, altitude);
    }
}
