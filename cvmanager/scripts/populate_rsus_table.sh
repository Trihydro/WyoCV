# Last updated 1/21/2024

# PGSQL info
db_name="postgres"
db_user="postgres"
db_password=$POSTGRES_PASSWORD
db_host="10.145.7.48"
db_port="5432"

# make
commsignia_model_id=1

# models
itsRs4M_model_id=1
rsu2xUsb_model_id=2

# rsu credentials
default_rsu_credential_id=1
commsignia_rsu_credential_id=3

# snmp credentials
default_snmp_credential_id=1

# snmp versions
fourDot1_snmp_version_id=1
twelve18_snmp_version_id=2

# firmware versions
y20_0_0_firmware_version_id=1
y_20_1_0_firmware_version_id=2
y_20_23_3_b168981_firmware_version_id=3
y_20_39_4_b205116_firmware_version_id=4

# organizations
wydot_organization_id=2

# read in values from .csv using awk
echo "Reading in values from .csv file..."
filename=$1
if [ -z "$filename" ]; then
    echo "Error: no .csv file specified"
    exit 1
fi
while IFS=, read -r latitude longitude milepost ipv4_address serial_number iss_scms primary_route make model rsu_credential snmp_credential snmp_version firmware_version target_firmware_version; do
    latitude=$latitude
    longitude=$longitude
    milepost=$milepost
    ipv4_address=$ipv4_address
    serial_number=$serial_number
    iss_scms=$iss_scms
    primary_route=$primary_route
    make=$make
    model=$model
    rsu_credential=$rsu_credential
    snmp_credential=$snmp_credential
    snmp_version=$snmp_version
    firmware_version=$firmware_version
    target_firmware_version=$target_firmware_version

    # if header, skip
    if [ "$latitude" = "latitude" ]; then
        echo "Header detected, skipping..."
        continue
    fi

    # if RSU is already in rsus table, skip it
    PGPASSWORD=$db_password psql -d $db_name -U $db_user -h $db_host -p $db_port -c "SELECT * FROM public.rsus WHERE serial_number='$serial_number';" | grep -q 0
    if [ $? -eq 0 ]; then
        echo "RSU '$serial_number' is already in rsus table, skipping..."
        continue
    fi

    # translate values to ids
    echo "Translating values to ids for model, rsu_credential, snmp_credential, snmp_version, firmware_version, and target_firmware_version..."
    
    # Model
    if [ "$model" = "ITS-RS4-M" ]; then
        model_id=$itsRs4M_model_id
    elif [ "$model" = "RSU-2xUSB" ]; then
        model_id=$rsu2xUsb_model_id
    else
        echo "Error: invalid model '$model'"
        exit 1
    fi

    # RSU Credential
    if [ "$rsu_credential" = "default" ]; then
        rsu_credential_id=$default_rsu_credential_id
    else
        echo "Error: invalid rsu_credential '$rsu_credential'"
        exit 1
    fi

    # SNMP Credential
    if [ "$snmp_credential" = "default" ]; then
        snmp_credential_id=$default_snmp_credential_id
    else
        echo "Error: invalid snmp_credential '$snmp_credential'"
        exit 1
    fi

    # SNMP Version
    if [ "$snmp_version" = "4.1" ]; then
        snmp_version_id=$fourDot1_snmp_version_id
    elif [ "$snmp_version" = "1218" ]; then
        snmp_version_id=$twelve18_snmp_version_id
    else
        echo "Error: invalid snmp_version '$snmp_version'"
        exit 1
    fi

    # Firmware Version
    if [ "$firmware_version" = "y20.0.0" ]; then
        firmware_version_id=$y20_0_0_firmware_version_id
    elif [ "$firmware_version" = "y20.1.0" ]; then
        firmware_version_id=$y_20_1_0_firmware_version_id
    elif [ "$firmware_version" = "y20.23.3-b168981" ]; then
        firmware_version_id=$y_20_23_3_b168981_firmware_version_id
    elif [ "$firmware_version" = "y20.39.4-b205116" ]; then
        firmware_version_id=$y_20_39_4_b205116_firmware_version_id
    else
        echo "Error: invalid firmware_version '$firmware_version'"
        exit 1
    fi

    # Target Firmware Version
    if [ "$target_firmware_version" = "y20.0.0" ]; then
        target_firmware_version_id=$y20_0_0_firmware_version_id
    elif [ "$target_firmware_version" = "y20.1.0" ]; then
        target_firmware_version_id=$y_20_1_0_firmware_version_id
    elif [ "$target_firmware_version" = "y20.23.3-b168981" ]; then
        target_firmware_version_id=$y_20_23_3_b168981_firmware_version_id
    elif [ "$target_firmware_version" = "y20.39.4-b205116" ]; then
        target_firmware_version_id=$y_20_39_4_b205116_firmware_version_id
    else
        echo "Error: invalid target_firmware_version '$target_firmware_version'"
        exit 1
    fi

    # print RSU info
    echo "Printing RSU info..."
    echo "----------------------------------------"
    echo "latitude: $latitude"
    echo "longitude: $longitude"
    echo "milepost: $milepost"
    echo "ipv4_address: $ipv4_address"
    echo "serial_number: $serial_number"
    echo "iss_scms: $iss_scms"
    echo "primary_route: $primary_route"
    echo "model: $model_id"
    echo "rsu_credential: $rsu_credential_id"
    echo "snmp_credential: $snmp_credential_id"
    echo "snmp_version: $snmp_version_id"
    echo "firmware_version: $firmware_version_id"
    echo "target_firmware_version: $target_firmware_version_id"
    echo "----------------------------------------"

    # add RSU to rsus table
    echo "Adding RSU to rsus table..."
    PGPASSWORD=$db_password psql -d $db_name -U $db_user -h $db_host -p $db_port -c "INSERT INTO public.rsus(geography, milepost, ipv4_address, serial_number, iss_scms_id, primary_route, model, credential_id, snmp_credential_id, snmp_version_id, firmware_version, target_firmware_version) VALUES (ST_GeomFromText('POINT($longitude $latitude)'), $milepost, '$ipv4_address', '$serial_number', '$iss_scms', '$primary_route', $model_id, $rsu_credential_id, $snmp_credential_id, $snmp_version_id, $firmware_version_id, $target_firmware_version_id);"

    # associate RSU with organization
    echo "Associating RSU with organization..."
    PGPASSWORD=$db_password psql -d $db_name -U $db_user -h $db_host -p $db_port -c "INSERT INTO public.rsu_organization(rsu_id, organization_id) VALUES ((SELECT rsu_id FROM public.rsus WHERE serial_number='$serial_number'), $wydot_organization_id);"

done < $filename